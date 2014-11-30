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
	}
	
	public void RegisterObject(Object o)
	{
		s.getKryo().register(o.getClass());
	}
	
	public void StartServer()
	{
		try {
			s.bind(54555, 54777);
		} catch (IOException e) {
			System.err.println("Error: Failed to Start Server.");
		}
		
		System.out.println("Server started");
		
		s.addListener(new Listener() {
			public void received(Connection connection, Object object) 
			{
				if (object instanceof CoreRequest) {
					System.out.println( ((CoreRequest)object).dat );
					connection.sendTCP( new CoreResponse("Get me a Soda, Cunt."));
				}
			}
		} );
	}
}
