package com.mygdx.game;

import terrain.Background;
import terrain.FogOfWar;
import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class MilitaryBase 
{
	public static final int LOGOCOUNT = 4;
	private static final int LOGOOFFSETX = -2;
	private static final int LOGOOFFSETY = 185;
	
	private static Texture tex;
	private static AnimTex flag;
	private static TextureRegion[] logos;
	
	private Color col;
	private int logo;
	private int xpos;
	private int ypos;
	
	public static void Release()
	{
		if (tex != null)
			tex.dispose();
		
		if (flag != null)
			flag.Release();
	}
	
	private static void LoadTex()
	{
		if (tex == null)
			tex = new Texture( Gdx.files.internal("img/base.png") );
		
		if (flag == null) {
			flag = new AnimTex("img/flag.png", 1, 3, 1);
			flag.NewAnimation(0, 3, 0, 2, 0.333f);
		}
		
		if (logos == null) {
			Texture tmp = new Texture( Gdx.files.internal("img/logos.png") );
			logos = TextureRegion.split(tmp, tmp.getWidth()/LOGOCOUNT, tmp.getHeight())[0];
		}
	}
	
	public MilitaryBase(int XPos, Terrain Ter, Color Col)
	{
		LoadTex();
		logo = 0;
		xpos = XPos;
		col = Col;
		
		// generate the base data
		ypos = Ter.GetMinHeight(xpos, xpos+tex.getWidth());
		int max = Ter.GetMaxHeight(xpos, xpos+tex.getWidth());
		Ter.CutRegion(xpos, ypos, tex.getWidth(), max-ypos);
		ypos = Game.WORLDH - Ter.GetHeight(0) - 3;
	}
	
	public void SetLogo(int Logo)
	{
		logo = Logo;
		
		if (logo < 0) logo = 0;
		if (logo >= LOGOCOUNT) logo = LOGOCOUNT-1;
	}
	
	public int GetLogo()
	{
		return logo;
	}
	
	public static int GetWidth()
	{
		LoadTex();
		return tex.getWidth();
	}
	
	public static int GetHeight()
	{
		LoadTex();
		return tex.getHeight();
	}
	
	public void DrawView(Camera Cam)
	{
		Vector2 pos = new Vector2(xpos, ypos);
		pos.x += tex.getWidth()/2;
		pos.y += tex.getHeight()/2;
		
		FogOfWar.AddVisibleRegion(Cam.GetRenderX(pos.x), 
				Cam.GetRenderY(pos.y), 600);
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam)
	{
		// draw the base
		Batch.setColor(col);
		Batch.draw(tex, Cam.GetRenderX(xpos), Cam.GetRenderY(ypos));
		
		// draw the flag 
		flag.UpdateClock();
		flag.Render(Batch, Cam, 0, new Vector2(xpos+GetWidth()-2, ypos), 1f, 1f);
		flag.Render(Batch, Cam, 0, new Vector2(xpos, ypos), 1f, 1f);
		
		// draw the flags logo
		Batch.setColor(Background.FGCOLOR);
		Batch.draw(logos[logo], Cam.GetRenderX(xpos+GetWidth()-2+LOGOOFFSETX),
				Cam.GetRenderY(ypos+LOGOOFFSETY));
		Batch.draw(logos[logo], Cam.GetRenderX(xpos+LOGOOFFSETX),
				Cam.GetRenderY(ypos+LOGOOFFSETY));
		Batch.setColor(Color.WHITE);
	}
}
