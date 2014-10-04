package ui;

import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Game;

public class PointSelect 
{
	private static final int POINTGAP = 32;
	private static Texture tex;
	
	private Terrain ter;
	private int startx;
	private int startwidth;
	private int targetx;
	
	private int maxx;
	private int minx;
	
	public PointSelect(Terrain Ter)
	{
		if (tex == null)
			tex = new Texture( Gdx.files.internal("img/point.png") );
		
		ter = Ter;
		startx = 0;
		targetx = 0;
		startwidth = 0;
		
		maxx = Game.WORLDW;
		minx = 0;
	}
	
	public void SetPos(int StartX, int StartWidth)
	{
		startx = StartX;
		startwidth = StartWidth;
	}
	
	public int GetTargetX()
	{
		return targetx;
	}
	
	public void SetMaxDist(int Dist)
	{
		int d = 0;
		maxx = startx+startwidth;
		int prevheight = ter.GetHeight(maxx);
		maxx++;
		
		while (d<Dist)
		{
			int height = ter.GetHeight(maxx);
			
			// add the distance traveled
			d += Math.sqrt( Math.pow(height-prevheight, 2) + 1 );
			prevheight = height;
			maxx++;
		}
		
		d = 0;
		minx = startx;
		prevheight = ter.GetHeight(minx);
		minx--;
		
		while (d<Dist)
		{
			int height = ter.GetHeight(minx);
			
			// add the distance traveled
			d += Math.sqrt( Math.pow(height-prevheight, 2) + 1 );
			prevheight = height;
			minx--;
		}
	}
	
	public void Update(Vector2 Campos)
	{
		// get the mouse pos and set the maximum
		targetx = Gdx.input.getX() + (int)Campos.x;
		targetx = Math.min(targetx, maxx);
		targetx = Math.max(targetx, minx);
	}
	
	private void DrawAt(SpriteBatch Batch, Vector2 Campos, int XPos)
	{
		int ypos = ter.GetHeight(XPos + tex.getWidth()/2);
		Batch.draw(tex, XPos - Campos.x, Game.WORLDH - ypos - Campos.y);
	}
	
	public void Draw(SpriteBatch Batch, Vector2 Campos)
	{
		Batch.setColor(1.0f, 1.0f, 1.0f, 0.5f);
		
		// draw each point
		if (targetx > startx+startwidth)
			for (int x=startx+startwidth; x<targetx; x+=POINTGAP)
				DrawAt(Batch, Campos, x);
		else if (targetx < startx)
			for (int x=startx; x>targetx; x-=POINTGAP)
				DrawAt(Batch, Campos, x);
		
		// draw an additional point at the cursors location
		int height = Game.WORLDH - ter.GetHeight(tex.getWidth()/2 + targetx) - (int)Campos.y;
		Batch.draw(tex, targetx-Campos.x, height);
		Batch.setColor(Color.WHITE);
	}
}
