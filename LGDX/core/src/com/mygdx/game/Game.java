package com.mygdx.game;

import physics.PhysicsWorld;
import terrain.SeedGenerator;
import terrain.Terrain;
import terrain.TerrainSeed;
import ammunition.CannonBall;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import entity.Army;
import entity.Gunman;
import entity.Squad;
import entity.Tank;

public class Game extends ApplicationAdapter 
{
	/*
	 *	Global Constants
	 */
	public static int WINDOWW = 960;
	public static int WINDOWH = 1200; 
	
	public static final int SCREENW = 960;
	public static final int SCREENH = 600;
	
	public static final int WORLDW = 1920*2;
	public static final int WORLDH = 1200;
	
	public static final int GRAVITY = 160;
	public static final int CAMSPEED = 512;
	
	/*
	 *	Local test data
	 *		most will eventually be moved into 
	 *		more generic classes
	 */
	private OrthographicCamera proj;
	private SpriteBatch batch;
	 
	private Background bg;
	private UI ui;
	private Camera cam;
	
	private PhysicsWorld physics;
	
	public Game(int WindowW, int WindowH)
	{
		WINDOWW = WindowW;
		WINDOWH = WindowH;
	}
	
	public void Release()
	{
		Shaders.Release();
		MilitaryBase.Release();
		CannonBall.Release();
		
		physics.Release();
		bg.Release();
		ui.Release();
	}
	
	@Override
	public void create() 
	{
		// initialize the shaders
		Shaders.InitShaders();
		Cursor.Init();
		
		// init the camera and the sprite batch
		proj = new OrthographicCamera();
		proj.setToOrtho(false, SCREENW, SCREENH);
		batch = new SpriteBatch();

		// generate the terrain
		TerrainSeed seed = SeedGenerator.GenerateSeed(WORLDW, WORLDH, 16);
		seed.AddBase(0, MilitaryBase.GetWidth());
		seed.AddBase(WORLDW/2, MilitaryBase.GetWidth());
		Terrain ter = new Terrain( seed );
		
		// init the bases
		Color bcol = new Color(16/255f, 16/255f, 16/255f, 1f);
		MilitaryBase b0 = new MilitaryBase( 0, ter, bcol );
		b0.SetLogo((int)(Math.random()*4));
		MilitaryBase b1 = new MilitaryBase( WORLDW/2, ter, bcol );
		
		if (b0.GetLogo() != 0)
		b1.SetLogo( b0.GetLogo()-1 );
		else b1.SetLogo(1);
		
		// create the camera with the position to follow
		cam = new Camera();
		cam.SetWorldMin( new Vector2(0.0f, 0.0f) );
		cam.SetWorldMax( new Vector2(WORLDW, WORLDH) );
		cam.SetPos( new Vector2(0, ter.GetHeight(0) - SCREENH/2) );
		
		// create the tank, background and ui
		bg = new Background();
		ui = new UI("img/ui.png");
		
		// create a line of gunman
		Squad gunmen = new Squad(ter);
		for (int i=0; i<5; i++)
		{
			Vector2 pos = new Vector2(512 + i*32, 0);
			gunmen.AddUnit( new Gunman(ter, pos, 40), cam );
		}
		
		// create the tank squad
		Squad tank = new Squad(ter);
		Tank add = new Tank("img/Tank1.png", "img/Barrel.png", ter, 20);
		add.SetBarrelOffset( new Vector2(17, 29) );
		tank.AddUnit(add, cam);
		
		// initialize the physics world
		physics = new PhysicsWorld(ter);
		
		Army a0 = new Army(b0);
		a0.AddSquad(gunmen);
		a0.AddSquad(tank);
		physics.AddArmy(a0);
		physics.AddArmy( new Army(b1) );
	}

	@Override
	public void render() 
	{
		FrameRate.Update();
		Cursor.Update();
		UpdateScene();
		
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
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
	}
	
	private void DrawScene()
	{
		bg.Draw(batch, (int)cam.GetPos().x);
		
		physics.Draw(batch, cam);
		
		ui.Draw(batch);
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
	}
	
	private void UpdateScene()
	{
		// update the camera position
		UpdatePos();
		
		physics.Update(cam);
	}
}
