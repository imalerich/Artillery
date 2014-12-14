package menu;

import network.NetworkManager;
import particles.Weather;
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
import com.mygdx.game.Camera;
import com.mygdx.game.Cursor;
import com.mygdx.game.Game;

public class MainMenu 
{
	private NetworkManager network;
	private Camera cam;
	
	private Texture button;
	private TextureRegion[] glyphs;
	private Rectangle[] bbox;
	
	private Texture loadingbar;
	private Texture greenbutton;
	private Texture redbutton;
	private Texture nullbutton;
	
	private double time = 0.0;
	private int lpos = 0;
	
	private static final int BUTTONDOWN = 4;
	private static final int SPACING = 4;
	
	private int status = WAITING;
	private static final int WAITING = 0;
	private static final int HOST = 1;
	private static final int CLIENT = 2;
	
	public MainMenu(NetworkManager Network)
	{
		network = Network;
		cam = new Camera();
		
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
		
		if (glyphs == null)
		{
			Texture tmp = new Texture( Gdx.files.internal("img/menu/network/glyphs.png") );
			glyphs = TextureRegion.split(tmp, button.getWidth(), button.getHeight())[0];
		}
		
		bbox = new Rectangle[2];
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
			if (Cursor.isMouseOver(bbox[i], cam.getPos()) && Cursor.isButtonJustReleased(Cursor.LEFT))
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
		Gdx.gl.glClearColor(Background.BGCOLOR.r, Background.BGCOLOR.g, Background.BGCOLOR.b, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Weather.draw(Batch, cam);
		
		if (status == WAITING) {
			drawWaiting(Batch);
		} else if (status == HOST) {
			drawHost(Batch);
		} else if (status == CLIENT) {
			if (network.getClient().isConnected())
				drawHost(Batch);
			else
				drawClient(Batch);
		}
	}
	
	private void drawWaiting(SpriteBatch Batch)
	{
		for (int i=0; i<bbox.length; i++) {
			int offset = 0;
			if (Cursor.isMouseOver(bbox[i], cam.getPos()) && Cursor.isButtonPressed(Cursor.LEFT))
				offset = BUTTONDOWN;

			Batch.draw(button, bbox[i].x, bbox[i].y-offset);
			Batch.draw(glyphs[i], bbox[i].x, bbox[i].y-offset);
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
		network.setGameWorld(physics, physics.getCam());
		
		try {
			// if host, dispatch armies to clients
			network.dispatchRemoteArmies();
			
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
