package network;

import java.io.IOException;
import java.net.InetAddress;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class Connect 
{
	private Client c;
	
	public Connect()
	{
		c = new Client();
		c.start();
		
		System.out.println("Client started.");
	}
	
	public void RegisterObject(Object o)
	{
		c.getKryo().register(o.getClass());
	}
	
	public void ConnectToServer()
	{
		try {
			InetAddress address = c.discoverHost(54777, 5000);
			
			c.connect(5000, address, 54555, 54777);
		} catch (IOException e) {
			System.err.println("Error: Failed to Connect to the Remote Server.");
		}
		
		c.sendTCP( new Request("I'm Derk, and I suck.") );
		
		c.addListener(new Listener() {
			public void received(Connection connection, Object object) 
			{
				if (object instanceof Response) {
					System.out.println( ((Response)object).dat );
				}
			}
		} );
	}
}
