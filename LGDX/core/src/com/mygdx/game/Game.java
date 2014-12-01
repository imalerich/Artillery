package com.mygdx.game;

import network.NetworkManager;
import particles.ParticleMask;
import particles.Weather;
import physics.CombatPacket;
import physics.GameWorld;
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
		ResizeScreen(WindowW, WindowH);
	}
	
	public void resize(int width, int height)
	{
		ResizeScreen(width, height);
		
		proj = new OrthographicCamera();
		proj.setToOrtho(false, SCREENW, SCREENH);
	}
	
	public void Init()
	{
		Terrain.SetColor( new Color(54/255f, 47/255f, 43/255f, 1f));
		
		Tank.Init();
		Gunman.Init();
		Squad.Init();
		Shaders.Init();
		Cursor.Init();
		Background.Init();
		FogOfWar.Init();
		UnitDeployer.Init();
		MenuBar.Init();
		CombatPacket.Init();
		Missile.Init();
		Profile.Init();
		PowerButtons.Init();
		ParticleMask.Init();
		Terrain.Init();
		Weather.Init();
		SquadConfigurations.Init();
	}
	
	public void Release()
	{
		Squad.Release();
		Shaders.Release();
		MilitaryBase.Release();
		Background.Release();
		MenuBar.Release();
		CombatPacket.Release();
		Missile.Release();
		Profile.Release();
		PowerButtons.Release();
		ParticleMask.Release();
		Weather.Release();
		
		physics.Release();
	}
	
	@Override
	public void create() 
	{
		// init the game
		Init();
		
		// init the camera and the sprite batch
		batch = new SpriteBatch();
		proj = new OrthographicCamera();
		proj.setToOrtho(false, SCREENW, SCREENH);

		// generate the terrain
		network = new NetworkManager();
		if (ISHOST)
			network.InitHost();
		network.InitClient();
		
		TerrainSeed seed = network.GetSeed();
		while (seed == null)
			seed = network.GetSeed();
		Terrain ter = new Terrain( seed );
		
		// create the camera
		cam = new Camera();
		cam.SetWorldMin( new Vector2(0.0f, 0.0f) );
		cam.SetWorldMax( new Vector2(WORLDW, WORLDH) );
		cam.SetPos( new Vector2(0, ter.GetHeight(0) - SCREENH/2) );
		
		// initialize the physics world
		physics = new GameWorld(ter);
		network.SetGameWorld(physics);
	
		try {
			// wait for the lobby to fill
			while (!network.IsLobbyFull()) {
				Thread.sleep(50);
			}
			
			// if host, dispatch armies to clients
			network.DispatchRemoteArmies();
			
			
			// wait for the client to recieve the armies
			while (!network.RecievedAllArmies()) {
				Thread.sleep(50);
			}
			
			network.ReadRemoteArmies();
		} catch (InterruptedException e) {
			System.err.println("Error: thread sleep interrupted.");
		}
	}
	
	public static OrthographicCamera GetProj()
	{
		return proj;
	}

	@Override
	public void render() 
	{
		FrameRate.Update();
		Cursor.Update();
		UpdateScene();
		
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		
		// begin rendering the scene
		batch.setProjectionMatrix(proj.combined);
		batch.begin();
		
		DrawScene();
		
		batch.end();
		
		// exit on escape key
		if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
			Release(); // release data
			Gdx.app.exit();
		}
		
		// clear input from CursorInput
		CursorInput.ClearInput();
	}
	
	private void DrawScene()
	{
		physics.Draw(batch, cam);
	}
	
	private void UpdatePos()
	{
		// move the camera with the mouse
		if (Cursor.isButtonPressed(Cursor.MIDDLE))
		{
			cam.MoveHorizontal( 6 * -Cursor.GetDeltaX() );
			cam.MoveVertical( 6 * Cursor.GetDeltaY() );
		}
		
		// move the camera with the keyboard
		if (Gdx.input.isKeyPressed(Keys.D))
			cam.MoveHorizontal( CAMSPEED * Gdx.graphics.getDeltaTime() );
		else if (Gdx.input.isKeyPressed(Keys.A))
			cam.MoveHorizontal( -CAMSPEED * Gdx.graphics.getDeltaTime() );
		
		if (Gdx.input.isKeyPressed(Keys.W))
			cam.MoveVertical( CAMSPEED * Gdx.graphics.getDeltaTime() );
		else if (Gdx.input.isKeyPressed(Keys.S))
			cam.MoveVertical( -CAMSPEED * Gdx.graphics.getDeltaTime() );
		
		if (Gdx.input.isKeyJustPressed(Keys.PLUS)) {
			ZoomScreen(-32);
		} else if (Gdx.input.isKeyJustPressed(Keys.MINUS)) {
			ZoomScreen(32);
		} else if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) && Cursor.getScrollDirection() == -1) {
			ZoomScreen(-32);
		} else if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) && Cursor.getScrollDirection() == 1) {
			ZoomScreen(32);
		}
	}
	
	private void ZoomScreen(int Zoom)
	{
		int prevh = SCREENH;
		int prevw = SCREENW;
		SCREENH += Zoom;
		SCREENH = Math.max(Math.min(SCREENH, WORLDH), MINHEIGHT);
		
		SCREENRATIOY = (float)SCREENH/WINDOWH;
		SCREENW = (int)(WINDOWW*SCREENRATIOY);
		SCREENRATIOX = (float)SCREENW/WINDOWW;
		proj.setToOrtho(false, SCREENW, SCREENH);
	
		cam.MoveVertical(-((SCREENH-prevh)/2) * SCREENRATIOY);
		cam.MoveHorizontal(-((SCREENW-prevw)/2) * SCREENRATIOX);
	}
	
	private void ResizeScreen(int Width, int Height)
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
	
	private void UpdateScene()
	{
		// update the camera position
		UpdatePos();
		
		physics.Update(cam);
	}
}
