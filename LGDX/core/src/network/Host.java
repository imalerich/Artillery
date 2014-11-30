package network;

import java.io.IOException;

import terrain.SeedGenerator;
import terrain.TerrainSeed;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.mygdx.game.Game;

public class Host 
{
	public final TerrainSeed seed;
	private Server s;
	
	public Host()
	{
		seed = SeedGenerator.GenerateSeed(Game.WORLDW, Game.WORLDH);
		s = new Server();
	}
	
	public Kryo GetKryo()
	{
		return s.getKryo();
	}
	
	public void StartServer()
	{
		s.addListener(new Listener() {
			public void connected(Connection connection) {
				System.out.println("Connected " + connection.toString());
				connection.sendTCP(seed);
				System.out.println("Terrain Seed sent to the client");
			}
			
			public void received(Connection connection, Object object)  {
				if (object instanceof Request) {
					System.out.println( ((Request)object).dat );
					Response r = new Response();
					r.dat = "Get me a Soda Cunt";
					connection.sendTCP(r); 
				} if (object instanceof Ping) {
					Ping p = new Ping();
					p.dat = "ping";
					connection.sendTCP(p);
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
