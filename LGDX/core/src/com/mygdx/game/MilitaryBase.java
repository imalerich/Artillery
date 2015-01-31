package com.mygdx.game;

import terrain.FogOfWar;
import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class MilitaryBase 
{
	public static Color BGCOLOR = new Color(112/255f, 107/255f, 98/255f, 1f);
	private static int VIEWRADIUS = 600;
	
	public static final int LOGOCOUNT = 4;
	public static final int LOGOOFFSETX = -2;
	public static final int LOGOOFFSETY = 185;
	private static final int MOUSEYTOLERANCE = 16;
	
	public static TextureRegion[] logos;
	private static Texture tex;
	private boolean hili = false;
	
	private int logo;
	private int xpos;
	private int ypos;
	
	public static void release()
	{
		if (tex != null)
			tex.dispose();
	}
	
	public static int getWidth()
	{
		loadTex();
		return tex.getWidth();
	}
	
	public static int getHeight()
	{
		loadTex();
		return tex.getHeight();
	}
	
	private static void loadTex()
	{
		if (tex == null)
			tex = new Texture( Gdx.files.internal("img/army/base.png") );
		
		if (logos == null) {
			Texture tmp = new Texture( Gdx.files.internal("img/army/logos.png") );
			logos = TextureRegion.split(tmp, tmp.getWidth()/LOGOCOUNT, tmp.getHeight())[0];
		}
	}
	
	public static TextureRegion getLogo(int Index)
	{
		if (logos == null)
			return null;
		
		return logos[Index];
	}
	
	public void setHili(boolean H)
	{
		hili = H;
	}
	
	public MilitaryBase(int XPos, Terrain Ter)
	{
		loadTex();
		logo = 0;
		xpos = XPos;
		
		// generate the base data
		ypos = Ter.getMaxHeight(xpos, xpos+tex.getWidth());
		int max = Ter.getMinHeight(xpos, xpos+tex.getWidth());
		Ter.cutRegion(xpos, ypos, tex.getWidth(), ypos-max);
		ypos = Game.WORLDH - Ter.getHeight(0) - 3;
	}
	
	public boolean isPointInBounds(Vector2 Pos)
	{
		float x0 = xpos + tex.getWidth()/2f - VIEWRADIUS;
		float x1 = xpos + tex.getWidth()/2f + VIEWRADIUS;
		
		if (Pos.x >= x0 && Pos.x <= x1)
			return true;
		else if (Pos.x + Game.WORLDW >= x0 && Pos.x + Game.WORLDW <= x1)
			return true;
		else if (Pos.x - Game.WORLDW >= x0 && Pos.x - Game.WORLDW <= x1)
			return true;
		else
			return false;
	}
	
	public boolean isMouseOver(Vector2 Campos)
	{
		Rectangle r = new Rectangle(xpos, ypos-MOUSEYTOLERANCE, getWidth(), getHeight()+MOUSEYTOLERANCE);
		return Cursor.isMouseOver(r, Campos);
	}
	
	public Vector2 getPos()
	{
		return new Vector2(xpos, ypos);
	}
	
	public float getMidX()
	{
		return xpos + tex.getWidth()/2f;
	}
	
	public void setLogo(int Logo)
	{
		logo = Logo;
		
		if (logo < 0) logo = 0;
		if (logo >= LOGOCOUNT) logo = LOGOCOUNT-1;
	}
	
	public int getLogo()
	{
		return logo;
	}
	
	private void drawOutline(SpriteBatch Batch, Camera Cam)
	{
		for (int x=-1; x<2; x++) {
			for (int y=-1; y<2; y++) {
				Vector2 Coords = new Vector2(xpos, ypos);
				Coords.x += x;
				Coords.y += y;
				
				Batch.draw(tex, Cam.getRenderX(Coords.x), Cam.getRenderY(Coords.y));
			}
		}
	}
	
	private void drawHighlight(SpriteBatch Batch, Camera Cam)
	{
		// draw a highlighted version of the sprite
		Shaders.setShader(Batch, Shaders.hili);
		drawOutline(Batch, Cam);
		Shaders.revertShader(Batch);
	}
	
	public void drawView(Camera Cam)
	{
		Vector2 pos = new Vector2(xpos, ypos);
		pos.x += tex.getWidth()/2;
		pos.y += tex.getHeight()/2;
		
		FogOfWar.addVisibleRegion(Cam.getRenderX(pos.x), 
				Cam.getRenderY(pos.y), VIEWRADIUS);
	}
	
	public void draw(SpriteBatch Batch, Camera Cam)
	{
		if (hili)
			drawHighlight(Batch, Cam);
		// draw the base
		Batch.setColor( BGCOLOR );
		Batch.draw(tex, Cam.getRenderX(xpos), Cam.getRenderY(ypos));
		
		// draw the flag 
		Batch.setColor( BGCOLOR );
	}
}
