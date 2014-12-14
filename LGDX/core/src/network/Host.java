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
	public final TerrainSeed seed;
	
	private int lobbysize;
	private Server s;
	
	public Host(int LobbySize)
	{
		lobbysize = LobbySize;
		seed = SeedGenerator.generateSeed(Game.WORLDW, Game.WORLDH);		
		
		int offset = Game.WORLDW/lobbysize;
		for (int i=0; i<lobbysize; i++) {
			seed.addBase(offset*i, MilitaryBase.getWidth());
		}
		
		s = new Server();
	}
	
	public void release()
	{
		s.close();
		System.out.println("Network closed.");
	}
	
	public Kryo getKryo()
	{
		return s.getKryo();
	}
	
	public Server getServer()
	{
		return s;
	}
	
	public void startServer()
	{
		s.addListener(new Listener() {
			public void connected(Connection connection) {
				System.out.println("Connection recieved from " + connection.toString());
					
				// do not let the lobby over fill
				if (s.getConnections().length > lobbysize) {
					connection.close();
					System.err.println("Connections full - disconnecting at " + connection.toString());
				} else {
					connection.sendTCP(seed);
				}
			}
			
			public void received(Connection connection, Object object)  {
				if (object instanceof Ping) {
					Ping p = new Ping();
					p.dat = "ping";
					connection.sendTCP(p);
					
					System.out.println("Ping from " + connection.toString());
					
				} else if (object instanceof Request) {
					Request r = (Request)object;
					if (r.req.equals("IsLobbyFull")) {
						Response res = new Response();
						res.request = r.req;
						res.b0 = isLobbyFull();
						connection.sendTCP(res);
						
					} else if (r.req.equals("LobbySize")) {
						Response res = new Response();
						res.request = r.req;
						res.i0 = lobbysize;
						connection.sendTCP(res);
					
					} else if (r.req.equals("LobbyCount")) {
						Response res = new Response();
						res.request = r.req;
						res.i0 = s.getConnections().length;
						connection.sendTCP(res);
					}
				} else if (object instanceof Response) {
					// pass the message to all other clients to be processed
					Response r = (Response)object;
					s.sendToAllExceptTCP(r.source, r);
					
				}
			}
			
			public void disconnected(Connection connection) {
				System.err.println("Disconnected " + connection.toString());
			}
		} );
		
		start();
	}
	
	public boolean isLobbyFull()
	{
		return s.getConnections().length >= lobbysize;
	}
	
	public void dispatchRemoteArmies()
	{
		// inform each client of all connected clients
		for (Connection c : s.getConnections()) {
			int pos = (c.getID()-1) * Game.WORLDW/lobbysize;
			int tankoffset = 70;
			ArmyConnection a = new ArmyConnection();
			a.pos = pos;
			a.tankoff = tankoffset;
			a.id = c.getID();

			s.sendToAllTCP(a);
		}
	}
	
	private void start()
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
