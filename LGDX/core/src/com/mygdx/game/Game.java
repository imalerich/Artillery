package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Game extends ApplicationAdapter {
	private OrthographicCamera cam;
	private SpriteBatch batch;
	 
	private Terrain ter;
	private Background bg;
	private UI ui;
	private Tank tank;
	
	private double clock = 0.0;
	
	public static final int SCREENW = 960;
	public static final int SCREENH = 600;
	
	public void Release()
	{
		ter.Release();
		bg.Release();
		ui.Release();
		tank.Release();
	}
	
	@Override
	public void create() {
		// init the camera and the sprite batch
		cam = new OrthographicCamera();
		cam.setToOrtho(false, SCREENW, SCREENH);
		batch = new SpriteBatch();
		
		// generate the terrain
		ter = new Terrain( SeedGenerator.GenerateSeed(SCREENW, SCREENH, 32) );
		
		// create the tank, background and ui
		bg = new Background("bg.png");
		ui = new UI("ui.png");
		tank = new Tank("Tank1.png", ter);
	}

	@Override
	public void render () {
		UpdateScene();
		
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		// begin rendering the scene
		batch.setProjectionMatrix(cam.combined);
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
		bg.Draw(batch);
		ter.Draw(batch);
		tank.Draw(batch);
		
		//ui.Draw(batch);
	}
	
	private void UpdatePos()
	{
		// move the tank based on the users input
		if (Gdx.input.isKeyPressed(Keys.RIGHT))
			tank.MoveRight();
		else if (Gdx.input.isKeyPressed(Keys.LEFT))
			tank.MoveLeft();
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
		ter.Invalidate();
	}
}
