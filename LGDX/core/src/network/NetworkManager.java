package network;

import java.util.Vector;

import physics.GameWorld;
import terrain.TerrainSeed;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.minlog.Log;

public class NetworkManager 
{
	private Host h;
	private Recipient c;
	
	public void SetGameWorld(GameWorld Game)
	{
		c.SetGameWorld(Game);
	}

	public void InitHost()
	{
		Log.set(Log.LEVEL_DEBUG);
		h = new Host(2);
		
		Kryo k = h.GetKryo();
		k.register(ArmyConnection.class);
		k.register(Request.class);
		k.register(Response.class);
		k.register(Ping.class);
		k.register(Vector.class);
		k.register(Integer.class);
		k.register(Vector2.class);
		k.register(TerrainSeed.class);
		
		h.StartServer();
		
		System.out.println("Host Started.\n");
	}
	
	public void InitClient()
	{
		Log.set(Log.LEVEL_DEBUG);
		c = new Recipient();
		
		Kryo k = c.GetKryo();
		k.register(ArmyConnection.class);
		k.register(Request.class);
		k.register(Response.class);
		k.register(Ping.class);
		k.register(Vector.class);
		k.register(Integer.class);
		k.register(Vector2.class);
		k.register(TerrainSeed.class);
		
		c.ConnectToServer();
	}
	
	public TerrainSeed GetSeed()
	{
		if (c != null)
			return c.GetSeed();
		else if (HasServer())
			return h.seed;
		else
			return null;
	}
	
	public boolean IsLobbyFull()
	{
		c.PollLobby();
		return c.IsLobbyFull();
	}
	
	public boolean RecievedAllArmies()
	{
		return c.ArmiesRecieved();
	}
	
	public void DispatchRemoteArmies()
	{
		if (HasServer())
			h.DispatchRemoteArmies();
	}
	
	public void ReadRemoteArmies()
	{
		c.ReadRemoteArmies();
	}
	
	public void Ping()
	{
		c.Ping();
	}
	
	public void UpdatePing()
	{
		c.UpdatePing();
	}
	
	public double GetPing()
	{
		return c.GetPing();
	}
	
	public boolean HasServer()
	{
		return h != null;
	}
}
