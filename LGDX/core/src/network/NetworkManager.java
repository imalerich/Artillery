package network;

public class NetworkManager 
{
	private static Host h;
	private static Connect c;
	
	public static void InitHost()
	{
		h = new Host();
		h.GetKryo().register(CoreRequest.class);
		h.GetKryo().register(CoreResponse.class);
		
		h.StartServer();
	}
	
	public static void InitClient()
	{
		c = new Connect();
		c.GetKryo().register(CoreRequest.class);
		c.GetKryo().register(CoreResponse.class);
		
		c.ConnectToServer();
	}
}
