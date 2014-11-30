package network;

import physics.GameWorld;
import terrain.TerrainSeed;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.minlog.Log;

public class NetworkManager 
{
	private Host h;
	private Recipient c;
	
	public NetworkManager(boolean IsHost)
	{
		if (IsHost) {
			InitHost();
			InitClient();
		} else {
			InitClient();
		}
	}
	
	public void SetGameWorld(GameWorld Game)
	{
		c.SetGameWorld(Game);
	}
	
	private void InitHost()
	{
		Log.set(Log.LEVEL_DEBUG);
		h = new Host();
		
		Kryo k = h.GetKryo();
		k.register(Integer.class);
		k.register(Ping.class);
		k.register(java.util.Vector.class);
		k.register(Integer.class);
		k.register(Vector2.class);
		k.register(TerrainSeed.class);
		
		h.StartServer();
		
		System.out.println("Host Started.");
	}
	
	private void InitClient()
	{
		Log.set(Log.LEVEL_DEBUG);
		c = new Recipient();
		
		Kryo k = c.GetKryo();
		k.register(Integer.class);
		k.register(Ping.class);
		k.register(java.util.Vector.class);
		k.register(Integer.class);
		k.register(Vector2.class);
		k.register(TerrainSeed.class);
		
		c.ConnectToServer();
		
		System.out.println("Client Connected.");
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
