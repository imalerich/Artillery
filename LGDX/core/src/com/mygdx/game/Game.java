package com.mygdx.game;

import network.NetworkManager;
import objects.FoxHole;
import objects.TankBarrier;
import particles.ParticleMask;
import particles.Weather;
import physics.CombatPacket;
import physics.GameWorld;
import physics.Grenade;
import physics.Missile;
import terrain.Background;
import terrain.FogOfWar;
import terrain.Terrain;
import terrain.TerrainSeed;
import ui.MenuBar;
import ui.PowerButtons;
import ui.Profile;
import ui.UnitDeployer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import config.SquadConfigurations;
import entity.Gunman;
import entity.Squad;
import entity.Tank;

public class Game extends ApplicationAdapter 
{
	/*
	 *	Global Constants
	 */
	private static final int MINHEIGHT = 400;
	private static final int MAXHEIGHT = 1200;
	private final boolean ISHOST;
	
	public static int WINDOWW =	960;
	public static int WINDOWH = 1200;
	public static float SCREENRATIOX = 1;
	public static float SCREENRATIOY = 1;
	
	public static int SCREENW = 960;
	public static int SCREENH = 800;
	
	public static final int WORLDW = (int)1920*2;
	public static final int WORLDH = 1200;
	
	public static final int GRAVITY	= 160;
	public static final int CAMSPEED = 512;
	
	private static OrthographicCamera proj;
	private static SpriteBatch batch;

	private Camera cam;
	private GameWorld physics;
	private NetworkManager network;
	
	public Game(int WindowW, int WindowH, boolean IsHost)
	{
		ISHOST = IsHost;
		resizeScreen(WindowW, WindowH);
	}
	
	public void resize(int width, int height)
	{
		resizeScreen(width, height);
		
		proj = new OrthographicCamera();
		proj.setToOrtho(false, SCREENW, SCREENH);
	}
	
	public void init()
	{
		Terrain.setColor( new Color(54/255f, 47/255f, 43/255f, 1f));
		
		Tank.init();
		Gunman.init();
		Squad.init();
		Shaders.init();
		Cursor.init();
		Background.init();
		FogOfWar.init();
		UnitDeployer.init();
		MenuBar.init();
		CombatPacket.init();
		Missile.init();
		Grenade.init();
		Profile.init();
		PowerButtons.init();
		ParticleMask.init();
		Terrain.init();
		Weather.init();
		SquadConfigurations.init();
		FoxHole.init();
		TankBarrier.init();
	}
	
	public void release()
	{
		Squad.release();
		Shaders.release();
		MilitaryBase.release();
		Background.release();
		MenuBar.release();
		CombatPacket.release();
		Missile.release();
		Grenade.release();
		Profile.release();
		PowerButtons.release();
		ParticleMask.release();
		Weather.release();
		FoxHole.release();
		TankBarrier.release();
		
		physics.release();
	}
	
	@Override
	public void create() 
	{
		// init the game
		init();
		
		// init the camera and the sprite batch
		batch = new SpriteBatch();
		proj = new OrthographicCamera();
		proj.setToOrtho(false, SCREENW, SCREENH);

		// generate the terrain
		network = new NetworkManager();
		if (ISHOST)
			network.initHost();
		network.initClient();
		
		TerrainSeed seed = network.getSeed();
		while (seed == null)
			seed = network.getSeed();
		Terrain ter = new Terrain( seed );
		
		// create the camera
		cam = new Camera();
		cam.setWorldMin( new Vector2(0.0f, 0.0f) );
		cam.setWorldMax( new Vector2(WORLDW, WORLDH) );
		cam.setPos( new Vector2(0, ter.getHeight(0) - SCREENH/2) );
		
		// initialize the physics world
		physics = new GameWorld(ter);
		network.setGameWorld(physics, cam);
	
		try {
			// wait for the lobby to fill
			while (!network.isLobbyFull()) {
				Thread.sleep(50);
			}
			
			// if host, dispatch armies to clients
			network.dispatchRemoteArmies();
			
			
			// wait for the client to recieve the armies
			while (!network.recievedAllArmies()) {
				Thread.sleep(50);
			}
			
			network.readRemoteArmies();
		} catch (InterruptedException e) {
			System.err.println("Error: thread sleep interrupted.");
		}
	}
	
	public static OrthographicCamera getProj()
	{
		return proj;
	}

	@Override
	public void render() 
	{
		FrameRate.update();
		Cursor.update();
		updateScene();
		
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		
		// begin rendering the scene
		batch.setProjectionMatrix(proj.combined);
		batch.begin();
		
		drawScene();
		
		batch.end();
		
		// exit on escape key
		if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
			release(); // release data
			Gdx.app.exit();
		}
		
		// clear input from CursorInput
		CursorInput.clearInput();
	}
	
	private void drawScene()
	{
		physics.draw(batch, cam);
	}
	
	private void updatePos()
	{
		// move the camera with the mouse
		if (Cursor.isButtonPressed(Cursor.MIDDLE))
		{
			cam.moveHorizontal( 6 * -Cursor.getDeltaX() );
			cam.moveVertical( 6 * Cursor.getDeltaY() );
		}
		
		// move the camera with the keyboard
		if (Gdx.input.isKeyPressed(Keys.D))
			cam.moveHorizontal( CAMSPEED * Gdx.graphics.getDeltaTime() );
		else if (Gdx.input.isKeyPressed(Keys.A))
			cam.moveHorizontal( -CAMSPEED * Gdx.graphics.getDeltaTime() );
		
		if (Gdx.input.isKeyPressed(Keys.W))
			cam.moveVertical( CAMSPEED * Gdx.graphics.getDeltaTime() );
		else if (Gdx.input.isKeyPressed(Keys.S))
			cam.moveVertical( -CAMSPEED * Gdx.graphics.getDeltaTime() );
		
		if (Gdx.input.isKeyJustPressed(Keys.PLUS)) {
			zoomScreen(-32);
		} else if (Gdx.input.isKeyJustPressed(Keys.MINUS)) {
			zoomScreen(32);
		} else if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) && Cursor.getScrollDirection() == -1) {
			zoomScreen(-32);
		} else if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) && Cursor.getScrollDirection() == 1) {
			zoomScreen(32);
		}
	}
	
	private void zoomScreen(int Zoom)
	{
		int prevh = SCREENH;
		int prevw = SCREENW;
		SCREENH += Zoom;
		SCREENH = Math.max(Math.min(SCREENH, WORLDH), MINHEIGHT);
		
		SCREENRATIOY = (float)SCREENH/WINDOWH;
		SCREENW = (int)(WINDOWW*SCREENRATIOY);
		SCREENRATIOX = (float)SCREENW/WINDOWW;
		proj.setToOrtho(false, SCREENW, SCREENH);
	
		cam.moveVertical(-((SCREENH-prevh)/2) * SCREENRATIOY);
		cam.moveHorizontal(-((SCREENW-prevw)/2) * SCREENRATIOX);
	}
	
	private void resizeScreen(int Width, int Height)
	{
		WINDOWW = Width;
		WINDOWH = Height;
		
		SCREENH = WINDOWH;
		if (SCREENH < MINHEIGHT)
			SCREENH = MINHEIGHT;
		else if (SCREENH>MAXHEIGHT)
			SCREENH /= (SCREENH/MAXHEIGHT + 1);
		
		SCREENRATIOY = (float)SCREENH/WINDOWH;
		
		SCREENW = (int)(WINDOWW*SCREENRATIOY);
		SCREENRATIOX = (float)SCREENW/WINDOWW;
	}
	
	private void updateScene()
	{
		// update the camera position
		updatePos();
		
		physics.update(cam);
	}
}
