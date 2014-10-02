package com.mygdx.game;

import java.util.Iterator;
import java.util.Vector;

import terrain.SeedGenerator;
import terrain.Terrain;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import entity.Gunman;
import entity.Tank;
import entity.WarPlane;

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
	private WarPlane plane;
	
	private Texture base;
	private int ypos;
	
	public static final int SCREENW = 960;
	public static final int SCREENH = 600;
	
	public static final int WORLDW = 1920*2;
	public static final int WORLDH = 1200;
	
	public void Release()
	{
		ter.Release();
		bg.Release();
		ui.Release();
		tank.Release();
		base.dispose();
	}
	
	@Override
	public void create() 
	{
		// init the camera and the sprite batch
		proj = new OrthographicCamera();
		proj.setToOrtho(false, SCREENW, SCREENH);
		batch = new SpriteBatch();

		// generate the terrain
		ter = new Terrain( SeedGenerator.GenerateSeed(WORLDW, WORLDH, 16) );
		
		// create the tank, background and ui
		bg = new Background();
		ui = new UI("ui.png");
		tank = new Tank("Tank1.png", "Barrel.png", ter, 60);
		tank.SetBarrelOffset( new Vector2(16, 32) );
		plane = new WarPlane(ter, 1200, SCREENH, 100);
		
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
		
		// generate the base
		base = new Texture( Gdx.files.internal("base.png") );
		ypos = ter.GetMinHeight(0, base.getWidth());
		int max = ter.GetMaxHeight(0, base.getWidth());
		ter.CutRegion(0, ypos, base.getWidth(), max-ypos);
		ypos = WORLDH - ter.GetHeight(0) - 3;
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
		plane.Draw(batch, cam.GetPos());
		
		batch.setColor(16/255.0f, 16/255.0f, 16/255.0f, 1);
		batch.draw(base, -cam.GetPos().x, ypos - 3 - cam.GetPos().y);
		batch.setColor(Color.WHITE);
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
		
		plane.MoveRight();
		
		if (Gdx.input.isKeyPressed(Keys.UP))
			tank.MoveBarrelUp();
		else if (Gdx.input.isKeyPressed(Keys.DOWN))
			tank.MoveBarrelDown();
		
		// set the camera to follow the tank
		if (Gdx.input.isButtonPressed(2))
		{
			cam.MoveHorizontal( 6 * -Gdx.input.getDeltaX() );
			cam.MoveVertical( 6 * Gdx.input.getDeltaY() );
		}
	}
	
	private void UpdateScene()
	{
		// update the tanks position
		UpdatePos();
		
		ter.Update();
	}
}
