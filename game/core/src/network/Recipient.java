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
import com.mygdx.game.Game;
import com.mygdx.game.MilitaryBase;

import config.ConfigSettings;
import config.SquadConfigs;
import entity.Classification;
import entity.Army;
import entity.RemoteArmy;
import entity.CompArmy;
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

		owned = addUserArmy();
		centerCameraAroundUser();
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

	public UserArmy addUserArmy()
	{
		UserArmy tmp = new UserArmy(game, baseForID(pos, id), game.getTerrain(), parent, c.getID());
		addStartupToArmy(tmp, pos, 70);
		game.setUserArmy(tmp);

		System.out.println("Creating user army with network ID: " + c.getID() + '.');
		return tmp;
	}
	
	public void addNetworkedArmy(int Pos, int TankOffset, int ID)
	{
		RemoteArmy army = new RemoteArmy(game, baseForID(Pos, ID), game.getTerrain(), parent, ID);
		addStartupToArmy(army, Pos, TankOffset);
		game.addEnemyArmy(army);
		
		System.out.println("\nCreating networked army with network ID: " + ID + '.');
	}

	public MilitaryBase baseForID(int Pos, int ID)
	{
		MilitaryBase base = new MilitaryBase(Pos, game.getTerrain());
		base.setLogo(ID-1);
		return base;
	}

	public void addStartupToArmy(Army A, int Pos, int TankOffset)
	{
		ConfigSettings tankSettings = SquadConfigs.getConfiguration(SquadConfigs.TANK);
		
		Squad squad = new Squad(game.getTerrain(), tankSettings.maxmovedist, A, Classification.TANK);
		squad.setPrimary(tankSettings.getFirstPrimary());
		squad.setArmor(tankSettings.getFirstArmor());
		
		Tank tank = new Tank("img/tanks/Tank1.png", game.getTerrain(), tankSettings.speed, tankSettings.health);
		tank.getPos( new Vector2(A.getBase().getPos().x + TankOffset, A.getBase().getPos().y) );
		tank.setBarrelOffset( new Vector2(17, 64-35) );
		squad.addUnit(tank);
		squad.setBarrelSrc( new Vector2(17, 64-35) );
		A.addSquad(squad);
	}

	public void centerCameraAroundUser()
	{
		// set the camera to center the base
		Vector2 campos = game.getCam().getPos();
		campos.x = owned.getBase().getMidX() - Game.SCREENW/2f;
		if (campos.x < 0) 
			campos.x += Game.WORLDW;
		else if (campos.x > Game.WORLDW) 
			campos.x -= Game.WORLDW;
		
		campos.y = (owned.getBase().getPos().y + MilitaryBase.getHeight()/2f) - (Game.SCREENH/2f);
		campos.y = Math.min(campos.y, Game.WORLDH-Game.SCREENH);
		campos.y = Math.max(campos.y, 0f);
		game.getCam().setPos(campos);
	}
}
