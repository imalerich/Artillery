package ui;

import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;

import config.SquadConfigs;
import entity.Army;
import entity.Gunman;

public class SpawnIndicator 
{
	private static final int SQUADSPACING = 24;
	private final TextureRegion tex;
	private final Terrain ter;
	private final int spawncount;
	private final int unittype;
	private final float width;
	private final float height;
	
	private int xpos;
	private boolean valid;
	private boolean forward = true;
	private float alpha = 0.6f;
	private Army a;
	
	public SpawnIndicator(Terrain Ter, Army A, int Type, int XPos)
	{
		ter = Ter;
		unittype = Type;
		valid = true;
		xpos = XPos;
		a = A;
		
		switch (unittype)
		{
		case UnitDeployer.GUNMAN:	
			spawncount = SquadConfigs.getConfiguration(SquadConfigs.GUNMAN).count;
			tex = TextureRegion.split(Gunman.GUNMAN, Gunman.GUNMAN.getWidth()/4, Gunman.GUNMAN.getHeight())[0][0];
			width = Gunman.GUNMAN.getWidth()/4;
			height = Gunman.GUNMAN.getHeight()/1;
			break;

		case UnitDeployer.STEALTHOPS:
			spawncount = SquadConfigs.getConfiguration(SquadConfigs.STEALTHOPS).count;
			tex = TextureRegion.split(Gunman.STEALTHTROOPS, Gunman.GUNMAN.getWidth()/4, Gunman.GUNMAN.getHeight())[0][0];
			width = Gunman.STEALTHTROOPS.getWidth()/4;
			height = Gunman.STEALTHTROOPS.getHeight()/1;
			break;

		case UnitDeployer.SPECOPS:
			spawncount = SquadConfigs.getConfiguration(SquadConfigs.SPECOPS).count;
			tex = TextureRegion.split(Gunman.SPECOPS, Gunman.GUNMAN.getWidth()/4, Gunman.GUNMAN.getHeight())[0][0];
			width = Gunman.SPECOPS.getWidth()/4;
			height = Gunman.SPECOPS.getHeight()/1;
			break;

		default:
			spawncount = 0;
			tex = null;
			width = 0f;
			height = 0f;
			break;
		}
		
		xpos -= (SQUADSPACING*spawncount)/2f;
	}
	
	public void setForward(boolean B)
	{
		forward = B;
	}
	
	public boolean isForward()
	{
		return forward;
	}
	
	public void setPos(int XPos)
	{
		xpos = XPos;
		xpos -= (SQUADSPACING*spawncount)/2f;
	}
	
	public Vector2 getPos()
	{
		float x = xpos + (SQUADSPACING*spawncount)/2f;
		float y = Game.WORLDH - ter.getHeight((int)x);
		
		return new Vector2(x, y);
	}
	
	public void setValid(boolean B)
	{
		valid = B;
	}
	
	public boolean isValid()
	{
		return valid;
	}
	
	private void setAlpha(float Target, float Rate)
	{
		if (alpha < Target) 
			alpha += Rate * Gdx.graphics.getDeltaTime();

		if (alpha > Target)
			alpha -= Rate * Gdx.graphics.getDeltaTime();
		
		if (Math.abs(alpha - Target) < 1/255f)
			alpha = Target;
	}
	
	public void draw(SpriteBatch Batch, Camera Cam)
	{
		if (tex == null)
			return;
		
		if (valid)
			setAlpha(0.6f, 1f);
		else
			setAlpha(0.2f, 2f);
		
		Batch.setColor(a.getColor().r, a.getColor().g, a.getColor().b, alpha);
		
		for (int i=0; i<spawncount; i++)
		{
			Vector2 pos = new Vector2(xpos + i*SQUADSPACING, 0);
			pos.y = Game.WORLDH - ter.getHeight((int)(pos.x + tex.getRegionWidth()/2)) - 3;
			
			float xscale = 1f;
			if (!forward) xscale = -1f;
			
			Batch.draw(tex, Cam.getRenderX(pos.x), Cam.getRenderY(pos.y), 
				width/2.0f, height/2.0f, width, height, xscale, 1f, 0.0f);
		}
		
		Batch.setColor(Color.WHITE);
	}
}
