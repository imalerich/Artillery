package ui;

import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Cursor;
import com.mygdx.game.Game;

public class PointSelect 
{
	private static final int POINTGAP = 24;
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
		if (targetx >= startx && targetx <= startx+startwidth)
			return -1;
		
		if (targetx <= (startx+startwidth)-Game.WORLDW && startx+startwidth >= Game.WORLDW)
			return -1;
		
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
		targetx = Cursor.GetMouseX(Campos) + (int)Campos.x;
		
		// check the distance to the target in each direction
		int rdist = (Game.WORLDW-(startx+startwidth))+targetx;
		if (targetx > startx+startwidth)
			rdist = targetx-(startx+startwidth);
		
		int ldist = startx + (Game.WORLDW-targetx);
		if (targetx < startx)
			ldist = (startx-targetx);
		
		if (rdist < ldist) {
			if (maxx < Game.WORLDW && targetx > maxx)
				targetx = maxx;
			else if (maxx > Game.WORLDW && targetx > (maxx-Game.WORLDW) &&
					(Gdx.input.getX()+Campos.x) > Game.WORLDW)
				targetx = (maxx-Game.WORLDW);
		} else {
			if (minx > 0 && targetx < minx && targetx < (startx+startwidth))
				targetx = minx;
			else if (minx < 0 && targetx < (minx+Game.WORLDW) &&
					targetx > (startx+startwidth))
				targetx = (minx+Game.WORLDW);
		}
		
		if (targetx == Game.WORLDW) targetx = 0;
	}
	
	private void DrawAt(SpriteBatch Batch, Camera Cam, int XPos)
	{
		int ypos = ter.GetHeight(XPos + tex.getWidth()/2);
		Batch.draw(tex, Cam.GetRenderX(XPos), Cam.GetRenderY(Game.WORLDH - ypos));
	}
	
	private void DrawLeft(SpriteBatch Batch, Camera Cam)
	{
		// render the points
		boolean next = true;
		int x = startx;
		
		while (next)
		{
			if (Math.abs(x - targetx) < POINTGAP)
				next = false;
			if (x < targetx && x+POINTGAP > targetx)
				break;
			
			DrawAt(Batch, Cam, x);
			
			x -= POINTGAP;
			if (x < 0) x += Game.WORLDW;
		}
	}
	
	private void DrawRight(SpriteBatch Batch, Camera Cam)
	{
		// render the points
		boolean next = true;
		int x = startx + startwidth;
		if (x > Game.WORLDW) x -= Game.WORLDW;
		
		while (next)
		{
			if (Math.abs(x - targetx) < POINTGAP)
				next = false;
			if (x > targetx && x-POINTGAP < targetx)
				break;
			
			DrawAt(Batch, Cam, x);
			
			x += POINTGAP;
			if (x > Game.WORLDW) x -= Game.WORLDW;
		}
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam)
	{
		if (targetx >= startx && targetx <= startx+startwidth)
			return;
		
		if (targetx <= (startx+startwidth)-Game.WORLDW && startx+startwidth >= Game.WORLDW)
			return;
		
		// check the distance to the target in each direction
		int rdist = (Game.WORLDW-(startx+startwidth))+targetx;
		if (targetx > startx+startwidth)
			rdist = targetx-(startx+startwidth);
		
		int ldist = startx + (Game.WORLDW-targetx);
		if (targetx < startx)
			ldist = (startx-targetx);
		
		if (rdist < ldist)
			DrawRight(Batch, Cam);
		else DrawLeft(Batch, Cam);
		
		// draw an additional point at the cursors location
		int height = Game.WORLDH - ter.GetHeight(tex.getWidth()/2 + targetx);
		Batch.draw(tex, Cam.GetRenderX(targetx), Cam.GetRenderY(height));
	}
}
