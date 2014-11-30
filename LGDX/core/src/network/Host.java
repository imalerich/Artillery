package network;

import java.io.IOException;

import terrain.SeedGenerator;
import terrain.TerrainSeed;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.mygdx.game.Game;
import com.mygdx.game.MilitaryBase;

public class Host 
{
	public static final int LOBBYSIZE = 2;
	public final TerrainSeed seed;
	
	private Server s;
	
	public Host()
	{
		seed = SeedGenerator.GenerateSeed(Game.WORLDW, Game.WORLDH);		
		
		int offset = Game.WORLDW/LOBBYSIZE;
		for (int i=0; i<LOBBYSIZE; i++) {
			seed.AddBase(offset*i, MilitaryBase.GetWidth());
		}
		
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
				connection.sendTCP(seed);
				System.out.println("Terrain seed sent to client at " + connection.toString());
			}
			
			public void received(Connection connection, Object object)  {
				if (object instanceof Ping) {
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
