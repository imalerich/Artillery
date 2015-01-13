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
	
	private int pos = -1;
	private int lobbysize = 0;
	private int lobbycount = 0;
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
	
	public void setGameWorld(GameWorld World)
	{
		// register the game world this host belongs to
		game = World;
		
		if (seed == null) {
			System.err.println("Error: Invalid Seed.");
			System.exit(1);
		}
		
		// add all of the outposts to the game world
		int id = 0;
		Iterator<Vector2> outposts = seed.getTowers();
		while (outposts.hasNext()) {
			id++;
			game.addOutpostMarker((int)outposts.next().x, id);
		}
	}
	
	public void setUserArmy()
	{
		// if we have not yet received the lobby size from the server, wait for it
		try {
			while (lobbysize == 0 || pos == -1) {
				Thread.sleep(50);
			}
			
		} catch (InterruptedException e) {
			System.err.println("Error: thread sleep interrupted.");
		}
		
		// add the hosts base to the game world
		ConfigSettings tankSettings = SquadConfigurations.getConfiguration(SquadConfigurations.TANK);
		
		MilitaryBase base = new MilitaryBase(pos, game.getTerrain());
		base.setLogo(id-1);
		owned = new UserArmy(game, base, game.getTerrain(), parent, c.getID());
		game.setUserArmy(owned);
		
		Squad squad = new Squad(game.getTerrain(), tankSettings.maxmovedist, owned);
		squad.setPrimary(tankSettings.getFirstPrimary());
		squad.setArmor(tankSettings.getFirstArmor());
		
		Tank tank = new Tank("img/tanks/Tank1.png", game.getTerrain(), tankSettings.speed, tankSettings.health);
		tank.getPos( new Vector2(base.getPos().x + 70, base.getPos().y) );
		tank.setBarrelOffset( new Vector2(17, 64-35) );
		squad.addUnit(tank);
		squad.setBarrelSrc( new Vector2(17, 64-35) );
		owned.addSquad(squad);
		
		// set the camera to center the base
		Vector2 campos = game.getCam().getPos();
		campos.x = pos;
		game.getCam().setPos(campos);
	}
	
	public void readRemoteArmies()
	{
		Iterator<ArmyConnection> i = remoteconnections.iterator();
		while (i.hasNext()) {
			ArmyConnection a = i.next();
			addNetworkedArmy(a.pos, a.tankoff, a.id);
		}
		
		game.requestFirstTurn();
	}
	
	public Kryo getKryo()
	{
		return c.getKryo();
	}
	
	public void connectToServer()
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
					} else {
						pos = a.pos;
					}
					
				} else if (object instanceof Response) {
					Response r = (Response)object;
					if (r.request.equals("IsLobbyFull")) {
						islobbyfull = r.b0;
						
					} else if (r.request.equals("LobbySize")) {
						lobbysize = r.i0;
						
					} else if (r.request.equals("LobbyCount")) {
						lobbycount = r.i0;
						
					}  else if (r.request.equals("NextTurn")) {
						game.setCurrentTurn(r.i0);
						
					} else if (r.source != -1) {
						// get the army it pertains to to process the message
						game.getRemoteArmy(r.source).catchMessage(r);
						
					}
				}
			}
			
			public void disconnected(Connection connection) {
				System.err.println("Disconnected");
				System.exit(1);
			}
			
		} );
		
		start();
	}
	
	public boolean armiesRecieved()
	{
		return remoteconnections.size() >= lobbysize-1;
	}
	
	public void pollLobby()
	{
		Request req = new Request();
		req.req = "IsLobbyFull";
		
		c.sendTCP(req);
		
		req = new Request();
		req.req = "LobbyCount";
		
		c.sendTCP(req);
	}
	
	public int getLobbyCount()
	{
		return lobbycount;
	}
	
	public int getLobbySize()
	{
		return lobbysize;
	}
	
	public boolean isLobbyFull()
	{
		return islobbyfull;
	}
	
	public void ping()
	{
		pingtime = 0.0;
		
		Ping p = new Ping();
		p.dat = "ping";
		c.sendTCP(p);
	}
	
	public void updatePing()
	{
		pingtime += Gdx.graphics.getDeltaTime();
	}
	
	public double getPing()
	{
		return ping;
	}
	
	public void start()
	{
		new Thread("Connect") {
			public void run() {
				while (true) {
					try {
						InetAddress address = null;
						while (address == null) {
							address = c.discoverHost(54777, 10000);
						}

						c.connect(10000, address, 54555, 54777);
						return;

					} catch (IOException e) {
						e.printStackTrace();
						System.err.println("Error: Failed to Connect to the Remote Server.");
					} 
				}
			}
		}.start();
	}
	
	public TerrainSeed getSeed()
	{
		return seed;
	}
	
	public Client getClient()
	{
		return c;
	}
	
	public void addNetworkedArmy(int Pos, int TankOffset, int ID)
	{
		// add the hosts base to the game world
		ConfigSettings tankSettings = SquadConfigurations.getConfiguration(SquadConfigurations.TANK);
		
		MilitaryBase base = new MilitaryBase(Pos, game.getTerrain());
		base.setLogo(id-1);
		RemoteArmy army = new RemoteArmy(game, base, game.getTerrain(), parent, ID);
		
		Squad squad = new Squad(game.getTerrain(), tankSettings.maxmovedist, army);
		squad.setPrimary(tankSettings.getFirstPrimary());
		squad.setArmor(tankSettings.getFirstArmor());
		
		Tank tank = new Tank("img/tanks/Tank1.png", game.getTerrain(), tankSettings.speed, tankSettings.health);
		tank.getPos( new Vector2(base.getPos().x + TankOffset, base.getPos().y) );
		tank.setBarrelOffset( new Vector2(17, 64-35) );
		squad.addUnit(tank);
		squad.setBarrelSrc( new Vector2(17, 64-35) );
		army.addSquad(squad);
		
		game.addEnemyArmy(army);
	}
}
