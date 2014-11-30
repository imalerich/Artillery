package network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.minlog.Log;

public class NetworkManager 
{
	private static Host h;
	private static Connect c;
	
	public static void InitHost()
	{
		Log.set(Log.LEVEL_DEBUG);
		h = new Host();
		
		Kryo k = h.GetKryo();
		k.register(Request.class);
		k.register(Response.class);
		
		h.StartServer();
		
		System.out.println("Host Started");
	}
	
	public static void InitClient()
	{
		Log.set(Log.LEVEL_DEBUG);
		c = new Connect();
		
		Kryo k = c.GetKryo();
		k.register(Request.class);
		k.register(Response.class);
		
		c.ConnectToServer();
		
		System.out.println("Client connected to Host");
	}
}
