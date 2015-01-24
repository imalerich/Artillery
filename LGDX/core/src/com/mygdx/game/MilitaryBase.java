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
	public static final Color BGCOLOR = new Color(112/255f, 107/255f, 98/255f, 1f);
	//public static final Color BGCOLOR = Terrain.getColor(); //new Color(56/255f, 17/255f, 14/255f, 1f);
	
	public static final int LOGOCOUNT = 4;
	public static final int LOGOOFFSETX = -2;
	public static final int LOGOOFFSETY = 185;
	private static final int MOUSEYTOLERANCE = 16;
	
	public static TextureRegion[] logos;
	private static Texture tex;
	
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
	
	public boolean isMouseOver(Vector2 Campos)
	{
		Rectangle r = new Rectangle(xpos, ypos-MOUSEYTOLERANCE, getWidth(), getHeight()+MOUSEYTOLERANCE);
		return Cursor.isMouseOver(r, Campos);
	}
	
	public Vector2 getPos()
	{
		return new Vector2(xpos, ypos);
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
	
	public void drawView(Camera Cam)
	{
		Vector2 pos = new Vector2(xpos, ypos);
		pos.x += tex.getWidth()/2;
		pos.y += tex.getHeight()/2;
		
		FogOfWar.addVisibleRegion(Cam.getRenderX(pos.x), 
				Cam.getRenderY(pos.y), 600);
	}
	
	
	public void draw(SpriteBatch Batch, Camera Cam)
	{
		// draw the base
		Batch.setColor( BGCOLOR );
		Batch.draw(tex, Cam.getRenderX(xpos), Cam.getRenderY(ypos));
		
		// draw the flag 
		Batch.setColor( BGCOLOR );
	}
}
