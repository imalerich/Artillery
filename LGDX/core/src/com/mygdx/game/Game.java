package com.mygdx.game;

import java.util.Iterator;
import java.util.Vector;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Game extends ApplicationAdapter 
{
	private OrthographicCamera proj;
	private SpriteBatch batch;
	 
	private Terrain ter;
	private Background bg;
	private UI ui;
	private Tank tank;
	private Camera cam;
	
	private Vector<Gunman> gunman;
	
	private double clock = 0.0;
	
	public static final int SCREENW = 960;
	public static final int SCREENH = 600;
	
	public static final int WORLDW = 1920;
	public static final int WORLDH = 1200;
	
	public void Release()
	{
		ter.Release();
		bg.Release();
		ui.Release();
		tank.Release();
	}
	
	@Override
	public void create() 
	{
		// init the camera and the sprite batch
		proj = new OrthographicCamera();
		proj.setToOrtho(false, SCREENW, SCREENH);
		batch = new SpriteBatch();
		
		// generate the terrain
		ter = new Terrain( SeedGenerator.GenerateSeed(WORLDW, WORLDH, 8) );
		
		// create the tank, background and ui
		bg = new Background();
		ui = new UI("ui.png");
		tank = new Tank("Tank1.png", ter, 60);
		
		// create a line of gunman
		gunman = new Vector<Gunman>();
		for (int i=0; i<15; i++)
		{
			Vector2 pos = new Vector2(i*32, 0);
			gunman.add( new Gunman(ter, pos, 40) );
		}
		
		// create the camera with the position to follow
		cam = new Camera();
		cam.SetWorldMin( new Vector2(0.0f, 0.0f) );
		cam.SetWorldMax( new Vector2(WORLDW, Float.MAX_VALUE) );
	}

	@Override
	public void render() 
	{
		FrameRate.Update();
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
		bg.Draw(batch, (int)tank.GetPos().x);
		ter.Draw(batch, cam.GetPos());
		tank.Draw(batch, cam.GetPos());
		
		Iterator<Gunman> i = gunman.iterator();
		while (i.hasNext()) 
			i.next().Draw(batch, cam.GetPos());
		
		ui.Draw(batch);
	}
	
	private void UpdatePos()
	{
		// move the tank based on the users input
		if (Gdx.input.isKeyPressed(Keys.RIGHT))
			tank.MoveRight();
		else if (Gdx.input.isKeyPressed(Keys.LEFT))
			tank.MoveLeft();
		
		Iterator<Gunman> i = gunman.iterator();
		while (i.hasNext())
			i.next().MoveRight();
		
		// set the camera to follow the tank
		Vector2 campos = new Vector2( tank.GetPos() );
		campos.x -= SCREENW/2 - 32;
		campos.y -= SCREENH/2 - 32;
		cam.SetPos( campos );
	}
	
	private void UpdateScene()
	{
		clock += Gdx.graphics.getDeltaTime();
	 	
		// update the tanks position
		UpdatePos();
		
		// update drawing holes all over the place
		if (clock >= 1.0) {
			int x = (int)(Math.random()*640);
			int y = ter.GetHeight(x);
			int r = (int)(Math.random()*16)+24;
			ter.AddHole(x, y, r);
			clock = 0.0;
		}
		
		ter.Update();
		ter.UpdateTex();
	}
}
