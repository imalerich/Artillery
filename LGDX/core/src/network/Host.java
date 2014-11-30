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
		Start();
		
		s.addListener(new Listener() {
			public void received(Connection connection, Object object) 
			{
				if (object instanceof CoreRequest) {
					System.out.println( ((CoreRequest)object).dat );
					connection.sendTCP( new CoreResponse("Get me a Soda, Cunt."));
				}
			}
		} );
	}
	
	private void Start()
	{
		s.start();
		
		try {
			s.bind(54555, 54777);
		} catch (IOException e) {
			System.err.println("Error: Failed to Start Server.");
		}
	}
}
