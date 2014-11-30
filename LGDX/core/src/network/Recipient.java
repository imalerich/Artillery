package network;

import java.io.IOException;
import java.net.InetAddress;

import physics.GameWorld;
import terrain.TerrainSeed;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.mygdx.game.Game;
import com.mygdx.game.MilitaryBase;

import config.ConfigSettings;
import config.SquadConfigurations;
import entity.Squad;
import entity.Tank;
import entity.UserArmy;

public class Recipient 
{
	private TerrainSeed seed;
	private GameWorld game;
	private Client c;
	
	private double pingtime = 0.0;
	private double ping = 0.0;
	private int id = 0;
	
	public Recipient()
	{
		c = new Client();
		c.start();
	}
	
	public void SetGameWorld(GameWorld World)
	{
		// register the game world this host belongs to
		game = World;
		
		System.out.println("Initializing Army at ID " + id);
		
		// add the hosts base to the game world
		ConfigSettings tankSettings = SquadConfigurations.GetConfiguration(SquadConfigurations.TANK);
		
		int offset = (id-1) * Game.WORLDW/Host.LOBBYSIZE;
		MilitaryBase base = new MilitaryBase(offset, game.GetTerrain());
		UserArmy army = new UserArmy(base, game.GetTerrain());
		game.SetUserArmy(army);
		
		Squad squad = new Squad(game.GetTerrain(), tankSettings.maxmovedist);
		squad.SetArmament(tankSettings.GetFirstArmament());
		squad.SetArmor(tankSettings.GetFirstArmor());
		
		Tank tank = new Tank("img/tanks/Tank1.png", game.GetTerrain(), tankSettings.speed, tankSettings.health);
		tank.SetPos( new Vector2(base.GetPos().x + 70, base.GetPos().y) );
		tank.SetBarrelOffset( new Vector2(17, 64-35) );
		squad.AddUnit(tank);
		squad.SetBarrelSrc( new Vector2(17, 64-35) );
		army.AddSquad(squad);
	}
	
	public Kryo GetKryo()
	{
		return c.getKryo();
	}
	
	public void ConnectToServer()
	{
		c.addListener( new Listener() {
			
			public void connected(Connection connection) {
				id = connection.getID();
				System.out.println("Connected to Host at " + connection.toString());
			}
			
			public void received(Connection connection, Object object)  {
				if (object instanceof TerrainSeed) {
					seed = (TerrainSeed)object;
					System.out.println("Terrain seed recieved from host.");
				} else if (object instanceof Ping) {
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
