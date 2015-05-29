package entity;

import terrain.Terrain;
import arsenal.Armament;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.AnimTex;
import com.mygdx.game.Camera;
import com.mygdx.game.Cursor;
import com.mygdx.game.Game;
import com.mygdx.game.Shaders;

public class Gunman extends Unit
{
	public static Texture DEATHANIM;
	public static Texture GUNMAN;
	public static Texture SPECOPS;
	public static Texture STEALTHTROOPS;
	public static Texture MORTAR;
	public static Texture MORTARUP;
	
	private AnimTex death;
	private AnimTex anim;
	private static int halfwidth;
	private float lasthealth;
	private double dmgclock;
	
	public static void init()
	{
		GUNMAN = new Texture( Gdx.files.internal("img/units/gunman.png") );
		STEALTHTROOPS = new Texture( Gdx.files.internal("img/units/stealthtroops.png") );
		SPECOPS = new Texture( Gdx.files.internal("img/units/specops.png") );
		DEATHANIM = new Texture( Gdx.files.internal("img/units/deathanim.png") );
		MORTAR = new Texture( Gdx.files.internal("img/units/mortar.png") );
		MORTARUP = new Texture( Gdx.files.internal("img/units/mortar_setup.png") );
	}
	
	@Override
	public void release()
	{
		if (GUNMAN != null)
			GUNMAN.dispose();
		
		if (STEALTHTROOPS != null)
			STEALTHTROOPS.dispose();
		
		if (SPECOPS != null)
			SPECOPS.dispose();
		
		if (DEATHANIM != null)
			DEATHANIM.dispose();
		
		if (MORTAR != null)
			MORTAR.dispose();
		
		if (MORTARUP != null)
			MORTARUP.dispose();
		
		anim.release();
	}
	
	@Override
	public boolean isAlive()
	{
		return !death.isCompleted(0);
	}
	
	public Gunman(Texture Tex, Terrain Ter, Vector2 Pos, int Speed, int Health, int ReqBonus)
	{
		if (anim == null) {
			anim = new AnimTex(Tex, 1, 4, 4);
			anim.newAnimation(0, 1, 0, 0, 0.0f);
			anim.newAnimation(1, 2, 0, 1, 0.2f);
			anim.newAnimation(2, 1, 2, 2, 0.0f);
			anim.newAnimation(3, 1, 3, 3, 0.0f);
		}
		
		if (death == null) {
			death = new AnimTex(DEATHANIM, 1, 3, 1);
			death.newAnimation(0, 3, 0, 2, 0.1f);
			death.setTime(0.0f);
		}
		
		setReqBonus(ReqBonus);
		halfwidth = anim.getFrameWidth();
		
		pos = Pos;
		pos.y = Game.WORLDH - Ter.getHeight((int)pos.x+halfwidth) - 3;
		
		width = anim.getFrameWidth();
		height = anim.getFrameHeight();
		
		forward = true;
		ter = Ter;
		speed = Speed;
		mugshotIndex = 0;
		health = Health;
		maxhealth = Health;
		lasthealth = Health;
		dmgclock = 0f;
	}
	
	private void drawOutline(SpriteBatch Batch, Camera Cam, int Index)
	{
		for (int x=-1; x<2; x++) {
			for (int y=-1; y<2; y++) {
				Vector2 Coords = new Vector2(pos);
				Coords.x += x;
				Coords.y += y;
				
				drawAnim(Batch, Cam, Index, Coords, width, height, false);
			}
		}
	}
	
	private void drawHighlight(SpriteBatch Batch, Camera Cam, int Index)
	{
		// draw a highlighted version of the sprite
		Shaders.setShader(Batch, Shaders.hili);
		drawOutline(Batch, Cam, Index);
		Shaders.revertShader(Batch);
	}
	
	private void drawTarget(SpriteBatch Batch, Camera Cam, int Index)
	{
		Shaders.setShader(Batch, Shaders.target);
		drawOutline(Batch, Cam, Index);
		Shaders.revertShader(Batch);
	}
	
	public void drawDieing(SpriteBatch Batch, Camera Cam)
	{
		death.updateClock();
		
		if (forward)
			death.render(Batch, Cam, 0, pos, 1.0f, 1.0f, false);
		else
			death.render(Batch, Cam, 0, pos, -1.0f, 1.0f, false);
	}
	
	@Override
	public void draw(SpriteBatch Batch, Camera Cam, boolean Highlight, boolean Target)
	{
		updateEmbers();
		
		if (health <= 0) {
			drawDieing(Batch, Cam);
			return;
		}
		
		setHeight();
		Vector2 Coords = new Vector2(pos);
		int index = 1;
		if (isFiring) {
			index = 2;
		} else if (lasthealth != health) {
			dmgclock += Gdx.graphics.getDeltaTime();
			index = 3;
			
			if (dmgclock > 0.4f) {
				lasthealth = health;
				dmgclock = 0f;
			}
		}

		if (moving)
			anim.updateClock();
		
		if (Highlight)
			drawHighlight(Batch, Cam, index);
		else if (Target)
			drawTarget(Batch, Cam, index);
		
		int width = anim.getFrameWidth();
		int height = anim.getFrameHeight();
		drawAnim(Batch, Cam, index, Coords, width, height, true);
		
		boolean drawhealth = true;
		if (getSquad().isStealthed() && !(getSquad().getArmy() instanceof UserArmy)) {
			drawhealth = false;
		}
		
		if (Cursor.isMouseOver(getBBox(), Cam.getPos()) && drawhealth) {
			int h = height;
			if (armor.getHealth() == 0) {
				Shaders.setShader(Batch, Shaders.health);
				h = (int)(height * (float)health/maxhealth);
			} else {
				Shaders.setShader(Batch, Shaders.armor);
				h = (int)(height * (float)armor.getHealth()/armor.getMaxHealth());
			}

			drawAnim(Batch, Cam, index, Coords, width, h, true);
			Shaders.revertShader(Batch);
		}
		
		moving = false;
	}
	
	private void drawAnim(SpriteBatch Batch, Camera Cam, int Index, Vector2 Coords, int SrcWidth, int SrcHeight, boolean setColor)
	{
		if (setColor) {
			Color c = getSquad().getArmy().unitcolor;
			float a = Batch.getColor().a;
			Batch.setColor( new Color(c.r, c.g, c.b, a));
		}
		
		if (getSquad().doSwapState()) {
			int h = (int)(MORTARUP.getHeight()* (float)health/maxhealth);
			Batch.draw(MORTARUP, Cam.getRenderX(Coords.x), Cam.getRenderY(Coords.y), 
					MORTARUP.getWidth(), h, 0, MORTARUP.getHeight()-h, MORTARUP.getWidth(), h, !forward, false);
		} else if (!getSquad().canMove()) {
			int h = (int)(MORTAR.getHeight()* (float)health/maxhealth);
			Batch.draw(MORTAR, Cam.getRenderX(Coords.x), Cam.getRenderY(Coords.y), 
					MORTAR.getWidth(), h, 0, MORTAR.getHeight()-h, MORTAR.getWidth(), h, !forward, false);
		} else {
			float dir = 1f;
			if (!forward) dir = -1f;
		
			anim.render(Batch, Cam, Index, Coords, dir, 1.0f, true, SrcWidth, SrcHeight);
		}
		
		float a = Batch.getColor().a;
		Batch.setColor( new Color(1, 1, 1, a) );
	}
	
	@Override
	public void drawTargetAngle(SpriteBatch Batch, Camera Cam)
	{
		float width = anim.getFrameWidth();
		float height = anim.getFrameHeight();
		int direction = 1;
		if (!forward)
			direction = -1;
		
		animtime += Gdx.graphics.getDeltaTime();
		Squad.target.setTime(animtime);
		
		if (getSquad().isFiringSecondary() && getSquad().getSecondary() != null && getSquad().getSecondary().getType() == Armament.POINTTARGET)
			Batch.draw(Squad.target.getCurrent(0), Cam.getRenderX(pos.x + width/2f + direction*width),
					Cam.getRenderY(pos.y + height/2f + width));
		else if (getSquad().isFiringOffhand() && getSquad().getOffhand() != null && getSquad().getOffhand().getType() == Armament.POINTTARGET)
			Batch.draw(Squad.target.getCurrent(0), Cam.getRenderX(pos.x + width/2f + direction*width),
					Cam.getRenderY(pos.y + height/2f + width));
		else if (getSquad().isFiringSecondary() && getSquad().getSecondary() != null && getSquad().getSecondary().getType() == Armament.LANDMINE)
			Batch.draw(Squad.target.getCurrent(0), Cam.getRenderX(pos.x + width/2f - Squad.target.getFrameWidth()/2f), 
					Cam.getRenderY(pos.y - Squad.target.getFrameHeight()));
		else if (getSquad().isFiringPrimary() && getSquad().getPrimary().getType() == Armament.FLAMETARGET)
			Batch.draw(Squad.target.getCurrent(0), Cam.getRenderX(pos.x + getForward() * (barrelsrc.x + 16f)), 
					Cam.getRenderY(pos.y + barrelsrc.y - Squad.target.getCurrent(0).getRegionHeight()/2f));
	}
}
