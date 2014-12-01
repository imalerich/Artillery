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
		seed = SeedGenerator.GenerateSeed(Game.WORLDW, Game.WORLDH);		
		
		int offset = Game.WORLDW/lobbysize;
		for (int i=0; i<lobbysize; i++) {
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
				System.out.println("Connection recieved from " + connection.toString());
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
						res.b = IsLobbyFull();
						connection.sendTCP(res);
						
					} else if (r.req.equals("LobbySize")) {
						Response res = new Response();
						res.request = r.req;
						res.i = lobbysize;
						connection.sendTCP(res);
						
					}
				}
			}
			
			public void disconnected(Connection connection) {
				System.err.println("Disconnected " + connection.toString());
			}
		} );
		
		Start();
	}
	
	public boolean IsLobbyFull()
	{
		return s.getConnections().length >= lobbysize;
	}
	
	public void DispatchRemoteArmies()
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
