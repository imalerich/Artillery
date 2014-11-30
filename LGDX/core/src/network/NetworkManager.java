package network;

import com.esotericsoftware.minlog.Log;

public class NetworkManager 
{
	private static Host h;
	private static Connect c;
	
	public static void InitHost()
	{
		Log.set(Log.LEVEL_DEBUG);
		h = new Host();
		h.GetKryo().register(CoreRequest.class);
		h.GetKryo().register(CoreResponse.class);
		
		h.StartServer();
	}
	
	public static void InitClient()
	{
		Log.set(Log.LEVEL_DEBUG);
		c = new Connect();
		c.GetKryo().register(CoreRequest.class);
		c.GetKryo().register(CoreResponse.class);
		
		c.ConnectToServer();
	}
}
