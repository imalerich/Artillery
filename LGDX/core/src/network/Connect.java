package network;

import java.io.IOException;
import java.net.InetAddress;

import com.badlogic.gdx.Gdx;
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
		c.start();
	}
	
	public Kryo GetKryo()
	{
		return c.getKryo();
	}
	
	public void ConnectToServer()
	{
		c.addListener( new Listener() {
			
			public void connected(Connection connection) {
				System.out.println("Connected to Host");
			}
			
			public void received(Connection connection, Object object)  {
				if (object instanceof Response) {
					System.out.println( ((Response)object).dat );
				}
			}
			
			public void disconnected(Connection connection) {
				System.err.println("Disconnected");
				Gdx.app.exit();
			}
			
		} );
		
		Start();
		Request r = new Request();
		r.dat = "Im Derk and I Suck.";
		c.sendTCP(r);
	}
	
	private void Start()
	{
		try {
			InetAddress address = c.discoverHost(54777, 5000);
			c.connect(5000, address, 54555, 54777);
			
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error: Failed to Connect to the Remote Server.");
			return;
		}
	}
}
