package network;

import java.io.IOException;
import java.net.InetAddress;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class Connect 
{
	private Client c;
	
	public Connect()
	{
		c = new Client();
	}
	
	public Kryo GetKryo()
	{
		return c.getKryo();
	}
	
	public void ConnectToServer()
	{
		c.start();
		
		try {
			InetAddress address = c.discoverHost(54777, 5000);
			
			c.connect(5000, address, 54555, 54777);
		} catch (IOException e) {
			System.err.println("Error: Failed to Connect to the Remote Server.");
			return;
		}
		
		System.out.println("Client started.");
		
		c.sendTCP( new CoreRequest("I'm Derk, and I suck.") );
		
		c.addListener(new Listener() {
			public void received(Connection connection, Object object) 
			{
				if (object instanceof CoreResponse) {
					System.out.println( ((CoreResponse)object).dat );
				}
			}
		} );
	}
}
