package menu;

import network.NetworkManager;
import physics.GameWorld;
import terrain.Background;
import terrain.Terrain;
import terrain.TerrainSeed;
import terrain.SeedGenerator;

import com.mygdx.game.MilitaryBase;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Cursor;
import com.mygdx.game.Game;

import config.ConfigSettings;
import config.SquadConfigs;
import entity.Classification;
import entity.Army;
import entity.RemoteArmy;
import entity.CompArmy;
import entity.Squad;
import entity.Tank;
import entity.UserArmy;

public class MainMenu 
{
	private NetworkManager network;
	private Camera cam;
	private Camera tmp;
	
	private Texture button;
	private TextureRegion[] glyphs;
	private Rectangle[] bbox;
	private Rectangle backbox;
	private Rectangle[] sizebox;
	
	private Texture loadingbar;
	private Texture greenbutton;
	private Texture redbutton;
	private Texture nullbutton;
	
	private Texture lobbyback;
	private Texture lobbyslot;
	private Texture lobbyadd;

	private int localLobbySize = 1;
	private double time = 0.0;
	private int lpos = 0;
	
	private static final int BUTTONDOWN = 4;
	private static final int SPACING = 2;
	
	private int status = WAITING;
	private static final int WAITING = 0;
	private static final int HOST = 1;
	private static final int CLIENT = 2;
	
	public MainMenu(NetworkManager Network)
	{
		network = Network;
		cam = new Camera();
		tmp = new Camera();
		
		if (button == null)
			button = new Texture( Gdx.files.internal("img/ui/indicators/button.png") );
		
		if (redbutton == null)
			redbutton = new Texture( Gdx.files.internal("img/menu/network/redbutton.png") );
		
		if (greenbutton == null)
			greenbutton = new Texture( Gdx.files.internal("img/menu/network/greenbutton.png") );
		
		if (nullbutton == null)
			nullbutton = new Texture( Gdx.files.internal("img/menu/network/nullbutton.png") );
		
		if (loadingbar == null)
			loadingbar = new Texture( Gdx.files.internal("img/menu/network/loadingbar.png") );
		
		if (lobbyback == null)
			lobbyback = new Texture( Gdx.files.internal("img/menu/network/lobby_size.png") );
		
		if (lobbyslot == null)
			lobbyslot = new Texture( Gdx.files.internal("img/menu/network/lobby_slot.png") );
		
		if (lobbyadd == null)
			lobbyadd = new Texture( Gdx.files.internal("img/menu/network/add_lobbysize.png") );
		
		if (glyphs == null)
		{
			Texture tmp = new Texture( Gdx.files.internal("img/menu/network/glyphs.png") );
			glyphs = TextureRegion.split(tmp, button.getWidth(), button.getHeight())[0];
		}
		
		bbox = new Rectangle[2];
		sizebox = new Rectangle[4];
	}
	
	public GameWorld update()
	{
		if (status == WAITING) {
			updateWaiting();
		} else if (status == HOST) {
			updateHost();
		} else if (status == CLIENT) {
			updateClient();
		}
		
		if (status != WAITING) {
			// the game is not networked, we can init the game right away
			if (!Game.NETWORKED) {
				return initGame();
			}

			// we are playing on a network so we must wait for people to connect
			if (network.isLobbyFull() && network.getUserClient() != null) {
				return initGame();
			}
		}
		
		return null;
	}
	
	private void updateWaiting()
	{
		calcBBox();
		int selected = -1;
		for (int i=0; i<bbox.length; i++) {
			if (Cursor.isMouseOver(bbox[i], tmp.getPos()) && Cursor.isButtonJustReleased(Cursor.LEFT))
				selected = i;
		}

		if (selected == 0) {
			setAsHost();
		} if (selected == 1) {
			setAsClient();
		}
	}
	
	private void updateHost()
	{
		//
	}
	
	private void updateClient()
	{
		//
	}
	
	public void draw(SpriteBatch Batch)
	{
		Gdx.gl.glClearColor(Background.NIGHT.r, Background.NIGHT.g, Background.NIGHT.b, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		//Weather.draw(Batch, cam);
		cam.setPos( new Vector2(cam.getPos().x, Game.WORLDH - Game.SCREENH) );
		cam.moveHorizontal( 64 * Gdx.graphics.getDeltaTime() );
		Background.update(cam);
		Background.drawBG(Batch, cam);
		
		if (status == WAITING) {
			drawWaiting(Batch);
		} else if (status == HOST) {
			drawHost(Batch);
		} else if (status == CLIENT) {
			if (!Game.NETWORKED) {
				drawHost(Batch);
			} else if (network.getUserClient().isConnected()) {
				drawHost(Batch);
			} else {
				drawClient(Batch);
			}
		}
	}
	
	private void drawWaiting(SpriteBatch Batch)
	{
		for (int i=0; i<bbox.length; i++) {
			int offset = 0;
			if (Cursor.isMouseOver(bbox[i], tmp.getPos()) && Cursor.isButtonPressed(Cursor.LEFT))
				offset = BUTTONDOWN;

			Batch.draw(button, bbox[i].x, bbox[i].y-offset);
			Batch.draw(glyphs[i], bbox[i].x, bbox[i].y-offset);
		}
		
		Batch.draw(lobbyback, backbox.x, backbox.y);
		for (int i=0; i<sizebox.length; i++) {
			float offsetx = lobbyslot.getWidth()/2f - lobbyadd.getWidth()/2f;
			float offsety = lobbyslot.getHeight()/2f - lobbyadd.getHeight()/2f;
			if (Cursor.isMouseOver(sizebox[i], tmp.getPos()) && Cursor.isButtonPressed(Cursor.LEFT))
				offsety -= 2;
			if (Cursor.isMouseOver(sizebox[i], tmp.getPos()) && Cursor.isButtonJustReleased(Cursor.LEFT)) {
				if (!Game.NETWORKED)
					localLobbySize = i+1;
				else
					network.setLobbySize(i+1);
			}
			
			Batch.draw(lobbyadd, sizebox[i].x + offsetx, sizebox[i].y + offsety);
		}
		
		for (int i=0; i < (Game.NETWORKED ? network.getLobbySize() : localLobbySize); i++) {
			Batch.draw(lobbyslot, sizebox[i].x, sizebox[i].y);
		}
	}
	
	private void drawHost(SpriteBatch Batch)
	{
		// used for red transparency
		time += Gdx.graphics.getDeltaTime()*2f;
		if (time > 2f) 
			time = 0f;
		
		// get position information
		float midx = Game.SCREENW/2f;
		float midy = Game.SCREENH/2f;
		float xpos = midx - loadingbar.getWidth();
		float ypos = midy - loadingbar.getHeight();
		
		// draw the buttons
		Batch.draw(loadingbar, xpos, ypos);
		
		int lobbysize = Game.NETWORKED ? network.lobbySize() : localLobbySize;
		int lobbycount = Game.NETWORKED ? network.lobbyConnected() : localLobbySize;
		
		for (int i=0; i<lobbycount; i++) {
			Batch.draw(greenbutton, xpos+3+(15*i), ypos+3);
		}
		
		for (int i=lobbycount; i<lobbysize; i++) {
			Batch.draw(nullbutton, xpos+3+(15*i), ypos+3);
			
			double trans = time > 1f ? 1f - (time - 1f) : time;
			
			Batch.setColor(1f, 1f, 1f, (float)trans);
			Batch.draw(redbutton, xpos+3+(15*i), ypos+3);
			Batch.setColor(Color.WHITE);
		}
	}
	
	private void drawClient(SpriteBatch Batch)
	{
		// used for red transparency
		time += Gdx.graphics.getDeltaTime()*3f;
		if (time > 2f) {
			time = 1f;
			lpos++;
			if (lpos == 4)
				lpos = 0;
		}
		
		// get position information
		float midx = Game.SCREENW/2f;
		float midy = Game.SCREENH/2f;
		float xpos = midx - loadingbar.getWidth();
		float ypos = midy - loadingbar.getHeight();
		
		// draw the buttons
		Batch.draw(loadingbar, xpos, ypos);
		
		float trans = (float)time;
		if (trans > 1f) {
			trans = 1f - (trans - 1f);
			
			int next = lpos+1;
			if (next == 4) next = 0;
			
			Batch.setColor(1f, 1f, 1f, trans);
			Batch.draw(redbutton, xpos+3+(15*lpos), ypos+3);
			
			Batch.setColor(1f, 1f, 1f, (float)time - 1f);
			Batch.draw(redbutton, xpos+3+(15*next), ypos+3);
			
		} else {
			Batch.setColor(1f, 1f, 1f, trans);
			Batch.draw(redbutton, xpos+3+(15*lpos), ypos+3);
		}
		
		Batch.setColor(Color.WHITE);
	}
	
	private void calcBBox()
	{
		float midx = Game.SCREENW/2f;
		float midy = Game.SCREENH/2f;
		
		float width = button.getWidth();
		float height = button.getHeight();
		
		bbox[0] = new Rectangle(midx-width-SPACING, midy-height/2f, width, height);
		bbox[1] = new Rectangle(midx+SPACING, midy-height/2f, width, height);
		
		backbox = new Rectangle(midx-lobbyback.getWidth()/2f, midy - height/2f - 3f - lobbyback.getHeight(), lobbyback.getWidth(), lobbyback.getHeight());
		sizebox[0] = new Rectangle(backbox.x + 2, backbox.y + lobbyslot.getHeight() + 7, lobbyslot.getWidth(), lobbyslot.getHeight());
		sizebox[1] = new Rectangle(backbox.x + lobbyslot.getWidth() + 3, backbox.y + lobbyslot.getHeight() + 7, lobbyslot.getWidth(), lobbyslot.getHeight());
		sizebox[2] = new Rectangle(backbox.x + 2, backbox.y + 6, lobbyslot.getWidth(), lobbyslot.getHeight());
		sizebox[3] = new Rectangle(backbox.x + lobbyslot.getWidth() + 3, backbox.y + 6, lobbyslot.getWidth(), lobbyslot.getHeight());
	}
	
	private void setAsHost()
	{
		status = HOST;

		if (Game.NETWORKED) {
			network.initHost();
			network.initUserClient();
		}
	}
	
	private void setAsClient()
	{
		status = CLIENT;

		if (Game.NETWORKED)
			network.initUserClient();
	}
	
	public GameWorld initGame()
	{
		// get the terrain from the network
		TerrainSeed seed = Game.NETWORKED ? network.getSeed() : null;
		while (seed == null && Game.NETWORKED)
			seed = network.getSeed();
		if (seed == null)
			seed = buildLocalSeed();

		Terrain ter = new Terrain( seed );
		
		// initialize the physics world
		GameWorld physics = new GameWorld(ter);
		if (network != null) 
			network.setGameWorld(physics);

		// If we are not networked initialize a non-networked game
		if (!Game.NETWORKED) {
			buildLocalArmies(physics);
			return physics;
		}
		
		try {
			// if host, dispatch armies to clients
			network.dispatchRemoteArmies();
			network.setUserArmy();
			
			// wait for the client to receive the armies
			while (!network.recievedAllArmies()) {
				Thread.sleep(50);
			}
			
			network.readRemoteArmies();
		} catch (InterruptedException e) {
			System.err.println("Error: thread sleep interrupted.");
		}
	
		return physics;
	}

	private TerrainSeed buildLocalSeed()
	{
		TerrainSeed seed = SeedGenerator.generateSeed(Game.WORLDW, Game.WORLDH);

		int offset = Game.WORLDW/localLobbySize;
		for (int i=0; i<localLobbySize; i++) {
			// will be used for an actual base
			seed.addBase(offset*i, MilitaryBase.getWidth());
		}

		return seed;
	}

	private void buildLocalArmies(GameWorld game)
	{
		addUserArmy(game);
	}

	private UserArmy addUserArmy(GameWorld game)
	{
		UserArmy tmp = new UserArmy(game, baseForID(game, 0, 1), game.getTerrain(), null, -1);
		addStartupToArmy(game, tmp, 0, 70);
		game.setUserArmy(tmp);

		System.out.println("Creating local user army.");
		return tmp;
	}

	public MilitaryBase baseForID(GameWorld game, int Pos, int ID)
	{
		MilitaryBase base = new MilitaryBase(0, game.getTerrain());
		base.setLogo(ID-1);
		return base;
	}

	private void addStartupToArmy(GameWorld game, Army A, int Pos, int TankOffset)
	{
		ConfigSettings tankSettings = SquadConfigs.getConfiguration(SquadConfigs.TANK);
		
		Squad squad = new Squad(game.getTerrain(), tankSettings.maxmovedist, A, Classification.TANK);
		squad.setPrimary(tankSettings.getFirstPrimary());
		squad.setArmor(tankSettings.getFirstArmor());
		
		Tank tank = new Tank("img/tanks/Tank1.png", game.getTerrain(), tankSettings.speed, tankSettings.health);
		tank.getPos( new Vector2(A.getBase().getPos().x + TankOffset, A.getBase().getPos().y) );
		tank.setBarrelOffset( new Vector2(17, 64-35) );
		squad.addUnit(tank);
		squad.setBarrelSrc( new Vector2(17, 64-35) );
		A.addSquad(squad);
	}
}
