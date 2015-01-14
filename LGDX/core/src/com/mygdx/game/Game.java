package com.mygdx.game;

import menu.MainMenu;
import network.NetworkManager;
import objects.FoxHole;
import objects.RadioTower;
import objects.TankBarrier;
import particles.ParticleMask;
import particles.Weather;
import physics.Blast;
import physics.CombatPacket;
import physics.GameWorld;
import physics.Grenade;
import physics.Missile;
import terrain.Background;
import terrain.FogOfWar;
import terrain.Terrain;
import ui.MenuBar;
import ui.OutpostFlag;
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
	
	public static int WINDOWW =	960;
	public static int WINDOWH = 1200;
	public static float SCREENRATIOX = 1;
	public static float SCREENRATIOY = 1;
	
	public static int SCREENW = 960;
	public static int SCREENH = 800;
	
	public static final int WORLDW = (int)1920*2;
	public static final int WORLDH = 1200;
	
	public static final int HOME = 0;
	public static final int GAME = 1;
	public static int stage = HOME;
	
	private static OrthographicCamera proj;
	private static SpriteBatch batch;

	private MainMenu menu;
	private GameWorld physics;
	private NetworkManager network;
	
	public Game(int WindowW, int WindowH)
	{
		resizeScreen(WindowW, WindowH);
	}
	
	public void resize(int width, int height)
	{
		resizeScreen(width, height);
		
		proj = new OrthographicCamera();
		proj.setToOrtho(false, SCREENW, SCREENH);
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
		
		network = new NetworkManager();
		menu = new MainMenu(network);
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
		updateZoom();
		
		update();
		
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		
		// begin rendering the scene
		batch.setProjectionMatrix(proj.combined);
		batch.begin();
		
		draw();
		
		batch.end();
		
		// exit on escape key
		if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
			release(); // release data
			Gdx.app.exit();
		}
		
		// clear input from CursorInput
		CursorInput.clearInput();
	}
	
	private void update()
	{
		switch (stage) {
		case HOME:
			physics = menu.update();
			
			// when the game world is received from the menu, set the stage to game
			if (physics != null) {
				stage = GAME;
			}
			
			return;
			
		case GAME:
			physics.update();
			return;
			
		default:
			return;
		}
	}
	
	private void draw()
	{
		switch (stage) {
		case HOME:
			menu.draw(batch);
			return;
			
		case GAME:
			physics.draw(batch);
			return;
			
		default:
			return;
		}
	}
		
	private void updateZoom() 
	{
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
	
		physics.getCam().moveVertical(-((SCREENH-prevh)/2) * SCREENRATIOY);
		physics.getCam().moveHorizontal(-((SCREENW-prevw)/2) * SCREENRATIOX);
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
		Blast.init();
		OutpostFlag.init();
		RadioTower.init();
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
		Blast.release();
		OutpostFlag.release();
		RadioTower.dispose();
		
		if (physics != null)
			physics.release();
	}
}
