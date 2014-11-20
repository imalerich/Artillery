package com.mygdx.game;

import particles.ParticleMask;
import particles.Weather;
import physics.CombatPacket;
import physics.GameWorld;
import physics.Missile;
import terrain.Background;
import terrain.FogOfWar;
import terrain.SeedGenerator;
import terrain.Terrain;
import terrain.TerrainSeed;
import ui.MenuBar;
import ui.PowerButtons;
import ui.Profile;
import ui.UnitDeployer;
import arsenal.Armament;
import arsenal.Armor;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import config.ConfigSettings;
import config.SquadConfigurations;
import entity.Army;
import entity.Gunman;
import entity.Squad;
import entity.Tank;
import entity.UserArmy;

public class Game extends ApplicationAdapter 
{
	/*
	 *	Global Constants
	 */
	private static final int MINHEIGHT = 400;
	private static final int MAXHEIGHT = 1200;
	
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
	
	public Game(int WindowW, int WindowH)
	{
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
		TerrainSeed seed = SeedGenerator.GenerateSeed(WORLDW, WORLDH);
		seed.AddBase(0, MilitaryBase.GetWidth());
		seed.AddBase(WORLDW/2, MilitaryBase.GetWidth());
		Terrain ter = new Terrain( seed );
		
		// init the bases
		MilitaryBase b0 = new MilitaryBase( 0, ter );
		b0.SetLogo((int)(Math.random()*4));
		MilitaryBase b1 = new MilitaryBase( WORLDW/2, ter );

		if (b0.GetLogo() != 0)
			b1.SetLogo( b0.GetLogo()-1 );
		else
			b1.SetLogo(1);
		
		// create the camera with the position to follow
		cam = new Camera();
		cam.SetWorldMin( new Vector2(0.0f, 0.0f) );
		cam.SetWorldMax( new Vector2(WORLDW, WORLDH) );
		cam.SetPos( new Vector2(0, ter.GetHeight(0) - SCREENH/2) );
		
		// initialize the physics world
		physics = new GameWorld(ter);
		
		UserArmy a0 = new UserArmy(b0, ter, cam);
		physics.SetUserArmy(a0);
		
		ConfigSettings tankSettings = SquadConfigurations.GetConfiguration(SquadConfigurations.TANK);
		
		Squad st0 = new Squad(ter, tankSettings.maxmovedist);
		st0.SetArmament(tankSettings.GetFirstArmament());
		st0.SetArmor(tankSettings.GetFirstArmor());
		
		Tank tank0 = new Tank("img/tanks/Tank1.png", ter, tankSettings.speed, tankSettings.health);
		tank0.SetBarrelOffset( new Vector2(17, 64-35) );
		st0.AddUnit(tank0, cam);
		st0.SetBarrelSrc( new Vector2(17, 64-35) );
		a0.AddSquad(st0);
		
		Army a1 = new Army(b1, ter);
		Squad st1 = new Squad(ter, tankSettings.maxmovedist);
		st1.SetArmament(tankSettings.GetFirstArmament());
		st1.SetArmor(tankSettings.GetFirstArmor());
		
		Tank tank1 = new Tank("img/tanks/Tank0.png", ter, tankSettings.speed, tankSettings.health);
		tank1.SetPos( new Vector2(b1.GetPos().x+70, b1.GetPos().y) );
		tank1.SetBarrelOffset( new Vector2(17, 29) );
		//tank1.SetBarrelOffset( new Vector2(17, 64-35) );
		//tank1.SetBarrelOffset( new Vector2(18, 64-36) );
		st1.AddUnit(tank1, cam);
		a1.AddSquad(st1);
		a1.SpawnUnit(UnitDeployer.GUNMAN, cam);
		physics.AddEnemyArmy(a1);
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
