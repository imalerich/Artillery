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
		k.register(CoreRequest.class);
		k.register(CoreResponse.class);
		
		h.StartServer();
	}
	
	public static void InitClient()
	{
		Log.set(Log.LEVEL_DEBUG);
		c = new Connect();
		
		Kryo k = c.GetKryo();
		k.register(CoreRequest.class);
		k.register(CoreResponse.class);
		
		c.ConnectToServer();
	}
}
