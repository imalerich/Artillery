package network;

import terrain.TerrainSeed;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.minlog.Log;

public class NetworkManager 
{
	private static final int NONE = -1;
	private static final int SERVER = 0;
	private static final int CLIENT = 1;
	private static int state = NONE;
	
	private static Host h;
	private static Recipient c;
	
	public static void InitHost()
	{
		Log.set(Log.LEVEL_DEBUG);
		h = new Host();
		
		Kryo k = h.GetKryo();
		k.register(String.class);
		k.register(Ping.class);
		k.register(java.util.Vector.class);
		k.register(Integer.class);
		k.register(Vector2.class);
		k.register(TerrainSeed.class);
		
		h.StartServer();
		
		System.out.println("Host Started");
		state = SERVER;
	}
	
	public static void InitClient()
	{
		Log.set(Log.LEVEL_DEBUG);
		c = new Recipient();
		
		Kryo k = c.GetKryo();
		k.register(String.class);
		k.register(Ping.class);
		k.register(java.util.Vector.class);
		k.register(Integer.class);
		k.register(Vector2.class);
		k.register(TerrainSeed.class);
		
		c.ConnectToServer();
		state = CLIENT;
	}
	
	public static TerrainSeed GetSeed()
	{
		if (IsClient())
			return c.GetSeed();
		else if (IsServer())
			return h.seed;
		else
			return null;
	}
	
	public static void Ping()
	{
		if (!IsClient())
			return;
		
		c.Ping();
	}
	
	public static void UpdatePing()
	{
		if (!IsClient())
			return;
		
		c.UpdatePing();
	}
	
	public static double GetPing()
	{
		if (!IsClient())
			return 0.0;
		
		return c.GetPing();
	}
	
	public static boolean IsClient()
	{
		return state == CLIENT;
	}
	
	public static boolean IsServer()
	{
		return state == SERVER;
	}
	
	public static Host GetServer()
	{
		return h;
	}
	
	public static Recipient GetClient()
	{
		return c;
	}
}
