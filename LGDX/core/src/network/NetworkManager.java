package network;

public class NetworkManager 
{
	private static Host h;
	private static Connect c;
	
	public static void InitHost()
	{
		h = new Host();
		h.RegisterObject(Request.class);
		h.RegisterObject(Response.class);
		
		h.StartServer();
	}
	
	public static void InitClient()
	{
		c = new Connect();
		c.RegisterObject(Request.class);
		c.RegisterObject(Response.class);
		
		c.ConnectToServer();
	}
}
