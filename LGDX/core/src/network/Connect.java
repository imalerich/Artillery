package network;

import java.io.IOException;
import java.net.InetAddress;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Listener.ThreadedListener;

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
		Start();
		
		c.addListener( new ThreadedListener( new Listener() {
			
			public void connected(Connection connection) {
				System.out.println("Connected to Host");
			}
			
			public void received(Connection connection, Object object)  {
				if (object instanceof CoreResponse) {
					System.out.println( ((CoreResponse)object).dat );
				}
			}
			
			public void disconnected(Connection connection) {
				Gdx.app.exit();
			}
			
		} ) );
		
		c.sendTCP( new CoreRequest("I'm Derk, and I suck.") );
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
