package network;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Vector;

import physics.GameWorld;
import terrain.TerrainSeed;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;
import com.mygdx.game.MilitaryBase;

import config.ConfigSettings;
import config.SquadConfigurations;
import entity.RemoteArmy;
import entity.Squad;
import entity.Tank;
import entity.UserArmy;

public class Recipient 
{
	private NetworkManager parent;
	private Vector<ArmyConnection> remoteconnections;
	private TerrainSeed seed;
	private GameWorld game;
	private Client c;
	
	private UserArmy owned;
	
	private int lobbysize = 0;
	private boolean islobbyfull = false;
	private double pingtime = 0.0;
	private double ping = 0.0;
	private int id = 0;
	
	public Recipient(NetworkManager Network)
	{
		parent = Network;
		remoteconnections = new Vector<ArmyConnection>();
		c = new Client();
		c.start();
	}
	
	public void SetGameWorld(GameWorld World, Camera Cam)
	{
		// if we have not yet received the lobby size from the server, wait for it
		try {
			while (lobbysize == 0) {
				Thread.sleep(50);
			}
		} catch (InterruptedException e) {
			System.err.println("Error: thread sleep interrupted.");
		}
		
		// register the game world this host belongs to
		game = World;
		
		System.out.println("Initializing Army at ID " + id);
		
		// add the hosts base to the game world
		ConfigSettings tankSettings = SquadConfigurations.GetConfiguration(SquadConfigurations.TANK);
		
		int offset = (id-1) * Game.WORLDW/lobbysize;
		MilitaryBase base = new MilitaryBase(offset, game.GetTerrain());
		owned = new UserArmy(base, game.GetTerrain(), parent, c.getID());
		game.SetUserArmy(owned);
		
		Squad squad = new Squad(game.GetTerrain(), tankSettings.maxmovedist);
		squad.SetArmament(tankSettings.GetFirstArmament());
		squad.SetArmor(tankSettings.GetFirstArmor());
		
		Tank tank = new Tank("img/tanks/Tank1.png", game.GetTerrain(), tankSettings.speed, tankSettings.health);
		tank.SetPos( new Vector2(base.GetPos().x + 70, base.GetPos().y) );
		tank.SetBarrelOffset( new Vector2(17, 64-35) );
		squad.AddUnit(tank);
		squad.SetBarrelSrc( new Vector2(17, 64-35) );
		owned.AddSquad(squad);
		
		// set the camera to center the base
		Vector2 campos = Cam.GetPos();
		campos.x = offset;
		Cam.SetPos(campos);
	}
	
	public void ReadRemoteArmies()
	{
		Iterator<ArmyConnection> i = remoteconnections.iterator();
		while (i.hasNext()) {
			System.out.println("Networked Army added to physics world");
			ArmyConnection a = i.next();
			AddNetworkedArmy(a.pos, a.tankoff, a.id);
		}
	}
	
	public Kryo GetKryo()
	{
		return c.getKryo();
	}
	
	public void ConnectToServer()
	{
		c.addListener( new Listener() {
			
			public void connected(Connection connection) {
				id = connection.getID();
				System.out.println("Connected to Host at " + connection.toString());
				
				// request the lobby size from the server
				Request r = new Request();
				r.req = "LobbySize";
				connection.sendTCP(r);
			}
			
			public void received(Connection connection, Object object)  {
				if (object instanceof TerrainSeed) {
					seed = (TerrainSeed)object;
					
				} else if (object instanceof Ping) {
					ping = (1000 * pingtime);
					
				} else if (object instanceof ArmyConnection) {
					ArmyConnection a = (ArmyConnection)object;
					
					if (a.id != id) {
						remoteconnections.add(new ArmyConnection(a));
					}
				} else if (object instanceof Response) {
					Response r = (Response)object;
					if (r.request.equals("IsLobbyFull")) {
						islobbyfull = r.b;
						
					} else if (r.request.equals("LobbySize")) {
						lobbysize = r.i;
						System.out.println("Lobby size is: " + r.i);
						
					} else if (r.source != -1) {
						// get the army it pertains to to process the message
						game.GetRemoteArmy(r.source).ProcMessage(r);
						
					}
				}
			}
			
			public void disconnected(Connection connection) {
				System.err.println("Disconnected");
				Gdx.app.exit();
			}
			
		} );
		
		Start();
	}
	
	public boolean ArmiesRecieved()
	{
		return remoteconnections.size() >= lobbysize-1;
	}
	
	public void PollLobby()
	{
		Request req = new Request();
		req.req = "IsLobbyFull";
		
		c.sendTCP(req);
	}
	
	public boolean IsLobbyFull()
	{
		return islobbyfull;
	}
	
	public void Ping()
	{
		pingtime = 0.0;
		
		Ping p = new Ping();
		p.dat = "ping";
		c.sendTCP(p);
	}
	
	public void UpdatePing()
	{
		pingtime += Gdx.graphics.getDeltaTime();
	}
	
	public double GetPing()
	{
		return ping;
	}
	
	private void Start()
	{
		try {
			InetAddress address = c.discoverHost(54777, 5000);
			c.connect(5000, address, 54555, 54777);
			
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error: Failed to Connect to the Remote Server.");
			return;
		}
	}
	
	public TerrainSeed GetSeed()
	{
		return seed;
	}
	
	public Client GetClient()
	{
		return c;
	}
	
	public void AddNetworkedArmy(int Pos, int TankOffset, int ID)
	{
		// add the hosts base to the game world
		ConfigSettings tankSettings = SquadConfigurations.GetConfiguration(SquadConfigurations.TANK);
		
		MilitaryBase base = new MilitaryBase(Pos, game.GetTerrain());
		RemoteArmy army = new RemoteArmy(base, game.GetTerrain(), parent, ID);
		
		Squad squad = new Squad(game.GetTerrain(), tankSettings.maxmovedist);
		squad.SetArmament(tankSettings.GetFirstArmament());
		squad.SetArmor(tankSettings.GetFirstArmor());
		
		Tank tank = new Tank("img/tanks/Tank1.png", game.GetTerrain(), tankSettings.speed, tankSettings.health);
		tank.SetPos( new Vector2(base.GetPos().x + TankOffset, base.GetPos().y) );
		tank.SetBarrelOffset( new Vector2(17, 64-35) );
		squad.AddUnit(tank);
		squad.SetBarrelSrc( new Vector2(17, 64-35) );
		army.AddSquad(squad);
		
		game.AddEnemyArmy(army);
	}
}
