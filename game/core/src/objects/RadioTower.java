package objects;

import java.util.Vector;
import physics.GameWorld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import com.mygdx.game.AnimTex;
import com.mygdx.game.Camera;
import com.mygdx.game.Cursor;
import com.mygdx.game.MilitaryBase;
import com.mygdx.game.Shaders;
import com.mygdx.game.Game;

import particles.Particles;
import physics.NullTank;
import config.SquadConfigs;
import entity.Squad;
import entity.Unit;
import entity.UserArmy;

public class RadioTower extends Unit
{
	public static Texture Tower;
	public static Texture Death;
	public static AnimTex Construction;
	private static Texture mortar;
	private static AnimTex flag;
	private AnimTex death;

	private boolean isBeingConstructed = true;
	private float stability = 1f;
	private float time = 0f;
	private int logo = 0;
	
	public static void init()
	{
		if (Tower == null)
			Tower = new Texture( Gdx.files.internal("img/objects/tower.png") );
		
		if (Death == null)
			Death = new Texture( Gdx.files.internal("img/objects/tower_fall_animation.png"));

		if (mortar == null)
			mortar = new Texture( Gdx.files.internal("img/objects/tower_mortar.png") );

		if (flag == null) {
			flag = new AnimTex("img/army/flag.png", 1, 3, 1);
			flag.newAnimation(0, 3, 0, 2, 0.333f/4f);
		}

		if (Construction == null) {
			Construction = new AnimTex("img/objects/tower_construction.png", 2, 8, 1);
			Construction.newAnimation(0, 11, 0, 10, 0.333f/3.5f);
		}
	}
	
	public static void dispose()
	{
		if (Tower != null)
			Tower.dispose();

		if (Death != null)
			Death.dispose();
		
		if (mortar != null)
			mortar.dispose();
		
		if (flag != null)
			flag.release();

		if (Construction != null)
			Construction.release();
	}

	@Override
	public boolean isAlive()
	{
		return !death.isCompleted(0);
	}

	@Override
	public void setAsDeceased(Vector<NullTank> Deceased, Particles Part)
	{
		// do not add a deceased troop to the terrain
		return;
	}

	public void updateStability()
	{
		// check what percentage of this tower is still on the ground
		int width = Tower.getWidth();
		int pointsOK = 0;

		for (int i = 0; i<width; i++) {
			int height = Game.WORLDH - ter.getHeight((int)pos.x + i) - 1;

			if (height + 2 > (int)pos.y)
				pointsOK++;
		}

		stability = (float)pointsOK/(float)width;

		// if this tower is no longer stable, destroy it
		if (stability < 0.6)
			health = 0;
	}

	public RadioTower(GameWorld World, Vector2 Pos, int Logo, boolean Construct)
	{
		this(World, Pos, Logo);
		isBeingConstructed = Construct;
		if (!isBeingConstructed)
			Construction.setTime(100f);
	}
	
	public RadioTower(GameWorld World, Vector2 Pos, int Logo)
	{
		setReqBonus(SquadConfigs.getConfiguration(SquadConfigs.TOWER).reqbonus);

		if (death == null) {
			death = new AnimTex(Death, 2, 8, 1);
			death.newAnimation(0, 16, 0, 15, 0.333f/4f);
			death.setTime(0f);
		}
	
	 	useFullWidthForY = true;
		logo = Logo;
		
		width = Tower.getWidth();
		height = Tower.getHeight();
		
		pos = new Vector2(Pos);
		forward = true;
		ter = World.getTerrain();
		speed = 0;
		mugshotIndex = 3;
	}

	public void drawDieing(SpriteBatch Batch, Camera Cam)
	{
		setHeight();
		death.updateClock();

		Batch.setColor(MilitaryBase.BGCOLOR);
		Vector2 v = new Vector2(pos.x - (death.getFrameWidth() - Tower.getWidth())/2f, pos.y);
		death.render(Batch, Cam, 0, v, 1.0f, 1.0f, false);
		Batch.setColor(Color.WHITE);
	}
	
	private void drawOutline(SpriteBatch Batch, Camera Cam)
	{
		for (int x=-1; x<2; x++) {
			for (int y=-1; y<2; y++) {
				Vector2 Coords = new Vector2(pos);
				Coords.x += x;
				Coords.y += y;
				
				flag.render(Batch, Cam, 0, new Vector2(Coords.x+6, Coords.y), 1f, 1f);
				Batch.draw(Tower, Cam.getRenderX(Coords.x), Cam.getRenderY(Coords.y));
				drawMortar(Batch, Cam, x , y);
			}
		}
	}
	
	private void drawTarget(SpriteBatch Batch, Camera Cam)
	{
		// draw a highlighted version of the sprite
		Shaders.setShader(Batch, Shaders.target);
		drawOutline(Batch, Cam);
		Shaders.revertShader(Batch);
	}
	
	private void drawHighlight(SpriteBatch Batch, Camera Cam)
	{
		// draw a highlighted version of the sprite
		Shaders.setShader(Batch, Shaders.hili);
		drawOutline(Batch, Cam);
		Shaders.revertShader(Batch);
	}
	
	private void drawLogo(SpriteBatch Batch, Camera Cam)
	{
		// draw the flags logo
		Batch.setColor(1.0f, 1.0f, 1.0f, 0.1f);
		Batch.draw(MilitaryBase.logos[logo], Cam.getRenderX(6+pos.x+MilitaryBase.LOGOOFFSETX),
				Cam.getRenderY(pos.y+MilitaryBase.LOGOOFFSETY));
		Batch.setColor(Color.WHITE);
	}
	
	@Override
	public void draw(SpriteBatch Batch, Camera Cam, boolean Highlight, boolean Target)
	{
		if (health <= 0) {
			drawDieing(Batch, Cam);
			return;
		}

		if (isBeingConstructed)
			Construction.setTime(time);
		time += Gdx.graphics.getDeltaTime();
		
		Batch.setColor(MilitaryBase.BGCOLOR);
		if (isBeingConstructed) {
			if (Construction.isCompleted(0)) {
				isBeingConstructed = false;
			} else {
				Vector2 v = new Vector2(pos.x - (Construction.getFrameWidth() - Tower.getWidth())/2f, pos.y);
				Construction.render(Batch, Cam, 0, v, 1.0f, 1.0f, false);
				Batch.setColor(Color.WHITE);
				return;
			}
		}


		flag.setTime(time);
		
		if (Highlight) {
			drawHighlight(Batch, Cam);
		} else if (Target) {
			drawTarget(Batch, Cam);
		}
		
		flag.render(Batch, Cam, 0, new Vector2(pos.x+6, pos.y), 1f, 1f);
		Batch.draw(Tower, Cam.getRenderX(pos.x), Cam.getRenderY(pos.y));
		drawMortar(Batch, Cam, 0 , 0);
		
		Batch.setColor(Color.WHITE);
		
		boolean drawhealth = true;
		if (getSquad().isStealthed() && !(getSquad().getArmy() instanceof UserArmy)) {
			drawhealth = false;
		}
		
		if (Cursor.isMouseOver(getBBox(), Cam.getPos()) &&  drawhealth) {
			Shaders.setShader(Batch, Shaders.health);
			int h = (int)(height * (float)health/maxhealth);
			Batch.draw(Tower, Cam.getRenderX(pos.x), Cam.getRenderY(pos.y), 
					Tower.getWidth(), h, 0, height-h, Tower.getWidth(), h, false, false);
			
			Shaders.revertShader(Batch);
		}

		drawLogo(Batch, Cam);
	}
	
	private void drawMortar(SpriteBatch Batch, Camera Cam, int offsetX, int offsetY)
	{
		if (forward)
			Batch.draw( mortar, Cam.getRenderX(pos.x + Tower.getWidth()-7 + offsetX), Cam.getRenderY(pos.y+131 + offsetY) );
		else
			Batch.draw( mortar, Cam.getRenderX(pos.x + 6 - mortar.getWidth() + offsetX), Cam.getRenderY(pos.y+131 + offsetY), 
					mortar.getWidth(), mortar.getHeight(), 0, 0, mortar.getWidth(), mortar.getHeight(), true, false);
	}
	
	@Override
	public void drawTargetAngle(SpriteBatch Batch, Camera Cam)
	{
		float offset = width;
		if (!forward) {
			offset = -offset;
		}
		
		animtime += Gdx.graphics.getDeltaTime();
		Squad.target.setTime(animtime);
		Batch.draw(Squad.target.getCurrent(0), Cam.getRenderX(pos.x + barrelsrc.x + offset), Cam.getRenderY(pos.y + barrelsrc.y));
	}
}
