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
	private Recipient c;
	
	public void setGameWorld(GameWorld Game)
	{
		c.setGameWorld(Game);
	}
	
	public void setUserArmy()
	{
		c.setUserArmy();
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
		
		h.startServer();
		
		System.out.println("Host Started.\n");
	}
	
	public void initClient()
	{
		Log.set(Log.LEVEL_DEBUG);
		c = new Recipient(this);
		
		Kryo k = c.getKryo();
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
		
		c.connectToServer();
	}
	
	public TerrainSeed getSeed()
	{
		if (c != null)
			return c.getSeed();
		else if (hasServer())
			return h.seed;
		else
			return null;
	}
	
	public boolean isLobbyFull()
	{
		if (c == null)
			return false;
		
		c.pollLobby();
		return c.isLobbyFull();
	}
	
	public int lobbySize()
	{
		if (c == null)
			return 0;
		
		c.pollLobby();
		return c.getLobbySize();
	}
	
	public int lobbyConnected()
	{
		if (c == null)
			return 0;
		
		c.pollLobby();
		return c.getLobbyCount();
	}
	
	public boolean recievedAllArmies()
	{
		return c.armiesRecieved();
	}
	
	public void dispatchRemoteArmies()
	{
		if (hasServer())
			h.dispatchRemoteArmies();
	}
	
	public void readRemoteArmies()
	{
		c.readRemoteArmies();
	}
	
	public void ping()
	{
		c.ping();
	}
	
	public void updatePing()
	{
		c.updatePing();
	}
	
	public double getPing()
	{
		return c.getPing();
	}
	
	public boolean hasServer()
	{
		return h != null;
	}
	
	public Server getServer()
	{
		return h.getServer();
	}
	
	public Client getClient()
	{
		return c.getClient();
	}
	
	public Recipient getRecipients()
	{
		return c;
	}
}
