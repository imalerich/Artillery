package network;

import java.util.Vector;

import physics.GameWorld;
import terrain.TerrainSeed;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

import entity.Army;

public class NetworkManager 
{
	private static final int MAXLOBBYSIZE = 4;
	private int lobbysize = 1;
	private Host h;
	private Recipient user;
	private Vector<Recipient> comp;
	
	public void setGameWorld(GameWorld Game)
	{
		user.setGameWorld(Game);
	}
	
	public void setUserArmy()
	{
		user.setUserArmy();
	}
	
	public int getLobbySize()
	{
		return lobbysize;
	}
	
	public void setLobbySize(int Size)
	{
		lobbysize = Size;
		if (lobbysize > MAXLOBBYSIZE)
			lobbysize = MAXLOBBYSIZE;
	}

	public void initHost()
	{
		Log.set(Log.LEVEL_DEBUG);
		h = new Host(lobbysize);
		
		Kryo k = h.getKryo();
		k.register(ArmyConnection.class);
		k.register(Connection.class);
		k.register(Connection[].class);
		k.register(Server.class);
		k.register(Army.class);
		k.register(Request.class);
		k.register(Response.class);
		k.register(Ping.class);
		k.register(Vector.class);
		k.register(Integer.class);
		k.register(Vector2.class);
		k.register(TerrainSeed.class);
		
		System.out.println("Host Started.\n");
		h.startServer();
	}
	
	public void initUserClient()
	{
		Log.set(Log.LEVEL_DEBUG);
		user = new Recipient(this);
		
		Kryo k = user.getKryo();
		k.register(ArmyConnection.class);
		k.register(Connection.class);
		k.register(Connection[].class);
		k.register(Server.class);
		k.register(Army.class);
		k.register(Request.class);
		k.register(Response.class);
		k.register(Ping.class);
		k.register(Vector.class);
		k.register(Integer.class);
		k.register(Vector2.class);
		k.register(TerrainSeed.class);
		
		System.out.println("Attempting to connect the user");
		user.connectToServer();
	}

	public void initCompClient()
	{
		Log.set(Log.LEVEL_DEBUG);
		Recipient tmp = new Recipient(this);

		Kryo k = tmp.getKryo();
		k.register(ArmyConnection.class);
		k.register(Connection.class);
		k.register(Connection[].class);
		k.register(Server.class);
		k.register(Army.class);
		k.register(Request.class);
		k.register(Response.class);
		k.register(Ping.class);
		k.register(Vector.class);
		k.register(Integer.class);
		k.register(Vector2.class);
		k.register(TerrainSeed.class);

		// try to connect the computer to the server
		System.out.println("Attempting to connect a new client for the AI");
		tmp.connectToServer();

		// init comp if necessary, then add the computer to the array
		comp = comp == null ? new Vector<Recipient>() : comp;
		comp.add(tmp);
	}
	
	public TerrainSeed getSeed()
	{
		if (user != null)
			return user.getSeed();
		else if (hasServer())
			return h.seed;
		else
			return null;
	}
	
	public boolean isLobbyFull()
	{
		if (user == null)
			return false;
		
		user.pollLobby();
		return user.isLobbyFull();
	}
	
	public int lobbySize()
	{
		if (user == null)
			return 0;
		
		user.pollLobby();
		return user.getLobbySize();
	}
	
	public int lobbyConnected()
	{
		if (user == null)
			return 0;
		
		user.pollLobby();
		return user.getLobbyCount();
	}
	
	public boolean recievedAllArmies()
	{
		return user.armiesRecieved();
	}
	
	public void dispatchRemoteArmies()
	{
		if (hasServer())
			h.dispatchRemoteArmies();
	}
	
	public void readRemoteArmies()
	{
		user.readRemoteArmies();
	}
	
	public void ping()
	{
		user.ping();
	}
	
	public void updatePing()
	{
		user.updatePing();
	}
	
	public double getPing()
	{
		return user.getPing();
	}
	
	public boolean hasServer()
	{
		return h != null;
	}
	
	public Server getServer()
	{
		return h.getServer();
	}
	
	public Client getUserClient()
	{
		return user.getClient();
	}

	public Client getCompClient(int index)
	{
		return comp.get(index).getClient();
	}
	
	public Recipient getUserRecipient()
	{
		return user;
	}

	public Vector<Recipient> getCompRecipients()
	{
		return comp;
	}
}
