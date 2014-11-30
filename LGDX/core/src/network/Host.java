package network;

import java.io.IOException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class Host 
{
	private Server s;
	
	public Host()
	{
		s = new Server();
	}
	
	public Kryo GetKryo()
	{
		return s.getKryo();
	}
	
	public void StartServer()
	{
		s.addListener(new Listener() {
			public void received(Connection connection, Object object)  {
				if (object instanceof Request) {
					System.out.println( ((Request)object).dat );
					connection.sendTCP( new Response("Get me a Soda, Cunt."));
				}
			}
			
			public void disconnected(Connection connection) {
				System.err.println("Disconnected " + connection.toString());
			}
		} );
		
		Start();
	}
	
	private void Start()
	{
		try {
			s.bind(54555, 54777);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error: Failed to Start Server.");
			return;
		}
		
		s.start();
	}
}
