package network;

public class NetworkManager 
{
	private static Host h;
	private static Connect c;
	
	public static void InitHost()
	{
		h = new Host();
		h.RegisterObject(CoreRequest.class);
		h.RegisterObject(CoreResponse.class);
		
		h.StartServer();
	}
	
	public static void InitClient()
	{
		c = new Connect();
		c.RegisterObject(CoreRequest.class);
		c.RegisterObject(CoreResponse.class);
		
		c.ConnectToServer();
	}
}
