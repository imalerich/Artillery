package menu;

import network.NetworkManager;
import physics.GameWorld;
import terrain.Background;
import terrain.Terrain;
import terrain.TerrainSeed;

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
			if (network.isLobbyFull() && network.getClient() != null) {
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
			if (network.getClient().isConnected()) {
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
			if (Cursor.isMouseOver(sizebox[i], tmp.getPos()) && Cursor.isButtonJustReleased(Cursor.LEFT))
				network.setLobbySize(i+1);
			
			Batch.draw(lobbyadd, sizebox[i].x + offsetx, sizebox[i].y + offsety);
		}
		
		for (int i=0; i<network.getLobbySize(); i++) {
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
		
		int lobbysize = network.lobbySize();
		int lobbycount = network.lobbyConnected();
		
		for (int i=0; i<lobbycount; i++) {
			Batch.draw(greenbutton, xpos+3+(15*i), ypos+3);
		}
		
		for (int i=lobbycount; i<lobbysize; i++) {
			Batch.draw(nullbutton, xpos+3+(15*i), ypos+3);
			
			float trans = (float)time;
			if (trans > 1f) {
				trans = 1f - (trans - 1f);
			}
			
			Batch.setColor(1f, 1f, 1f, trans);
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
		network.initHost();
		network.initClient();
	}
	
	private void setAsClient()
	{
		status = CLIENT;
		network.initClient();
	}
	
	public GameWorld initGame()
	{
		// get the terrain from the network
		TerrainSeed seed = network.getSeed();
		while (seed == null)
			seed = network.getSeed();
		Terrain ter = new Terrain( seed );
		
		// initialize the physics world
		GameWorld physics = new GameWorld(ter);
		network.setGameWorld(physics);
		
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
}
