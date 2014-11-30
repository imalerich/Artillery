package network;

import java.io.IOException;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class Host 
{
	private Server s;
	
	public Host()
	{
		s = new Server();
		s.start();
		
		try {
			s.bind(54555, 54777);
		} catch (IOException e) {
			System.err.println("Error: Failed to Start Server.");
		}
		
		System.out.println("Server started");
	}
	
	public void RegisterObject(Object o)
	{
		s.getKryo().register(o.getClass());
	}
	
	public void StartServer()
	{
		s.addListener(new Listener() {
			public void received(Connection connection, Object object) 
			{
				if (object instanceof Request) {
					System.out.println( ((Request)object).dat );
					connection.sendTCP( new Response("Get me a Soda, Cunt."));
				}
			}
		} );
	}
}
