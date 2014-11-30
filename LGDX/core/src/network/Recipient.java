package network;

import java.io.IOException;
import java.net.InetAddress;

import terrain.TerrainSeed;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class Recipient 
{
	private TerrainSeed seed;
	private Client c;
	
	private double pingtime = 0.0;
	private double ping = 0.0;
	
	public Recipient()
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
				if (object instanceof Ping) {
					ping = (1000 * pingtime);
				}
			}
			
			public void disconnected(Connection connection) {
				System.err.println("Disconnected");
				Gdx.app.exit();
			}
			
		} );
		
		Start();
	}
	
	public void Ping()
	{
		pingtime = 0.0;
		
		Ping p = new Ping();
		p.dat = "ping";
		c.sendTCP(p);
	}
	
	public void UpdatePing()
	{
		pingtime += Gdx.graphics.getDeltaTime();
	}
	
	public double GetPing()
	{
		return ping;
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
	
	public TerrainSeed GetSeed()
	{
		return seed;
	}
}
