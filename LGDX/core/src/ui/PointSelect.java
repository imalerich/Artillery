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
			tex = new Texture( Gdx.files.internal("img/ui/indicators/point.png") );
		
		ter = Ter;
		startx = 0;
		targetx = 0;
		startwidth = 0;
		
		maxx = Game.WORLDW;
		minx = 0;
	}
	
	public void setPos(int StartX, int StartWidth)
	{
		startx = StartX;
		startwidth = StartWidth;
	}
	
	public int getTargetX()
	{
		if (targetx >= startx && targetx <= startx+startwidth)
			return -1;
		
		if (targetx <= (startx+startwidth)-Game.WORLDW && startx+startwidth >= Game.WORLDW)
			return -1;
		
		return targetx;
	}
	
	public void setMaxDist(int Dist)
	{
		int d = 0;
		maxx = startx+startwidth;
		int prevheight = ter.getHeight(maxx);
		maxx++;
		
		while (d<Dist)
		{
			int height = ter.getHeight(maxx);
			
			// add the distance traveled
			d += Math.sqrt( Math.pow(height-prevheight, 2) + 1 );
			prevheight = height;
			maxx++;
		}
		
		d = 0;
		minx = startx;
		prevheight = ter.getHeight(minx);
		minx--;
		
		while (d<Dist)
		{
			int height = ter.getHeight(minx);
			
			// add the distance traveled
			d += Math.sqrt( Math.pow(height-prevheight, 2) + 1 );
			prevheight = height;
			minx--;
		}
	}
	
	public int getMaxX()
	{
		return maxx;
	}
	
	public int getMinX()
	{
		return minx;
	}
	
	public void update(Vector2 Campos)
	{
		// get the mouse pos and set the maximum
		targetx = Cursor.getMouseX(Campos) + (int)Campos.x;
		
		// check the distance to the target in each direction
		int rdist = (Game.WORLDW-(startx+startwidth))+targetx;
		if (targetx > startx+startwidth)
			rdist = targetx-(startx+startwidth);
		
		int ldist = startx + (Game.WORLDW-targetx);
		if (targetx < startx)
			ldist = (startx-targetx);
		
		if (rdist < ldist) {
			// right
			if (maxx < Game.WORLDW && targetx > maxx) {
				targetx = maxx;
			} else if (maxx >= Game.WORLDW) {
				if (targetx > startx && targetx < Game.WORLDW) {
					//
				} else if (targetx < startx && targetx > maxx - Game.WORLDW){
					targetx = maxx - Game.WORLDW;
				}
			}
			
		} else {
			// left
			if (minx > 0 && targetx < minx && targetx < (startx+startwidth)) {
				targetx = minx;
			} else if (minx < 0 && targetx < (minx+Game.WORLDW) &&
					targetx > (startx+startwidth)) {
				targetx = (minx+Game.WORLDW);
			}
		}
		
		if (targetx == Game.WORLDW) targetx = 0;
	}
	
	private void drawAt(SpriteBatch Batch, Camera Cam, int XPos)
	{
		int ypos = ter.getHeight(XPos + tex.getWidth()/2);
		Batch.draw(tex, Cam.getRenderX(XPos), Cam.getRenderY(Game.WORLDH - ypos));
	}
	
	private void drawLeft(SpriteBatch Batch, Camera Cam)
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
			
			drawAt(Batch, Cam, x);
			
			x -= POINTGAP;
			if (x < 0) x += Game.WORLDW;
		}
	}
	
	private void drawRight(SpriteBatch Batch, Camera Cam)
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
			
			drawAt(Batch, Cam, x);
			
			x += POINTGAP;
			if (x > Game.WORLDW) x -= Game.WORLDW;
		}
	}
	
	public void draw(SpriteBatch Batch, Camera Cam)
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
			drawRight(Batch, Cam);
		else drawLeft(Batch, Cam);
		
		// draw an additional point at the cursors location
		int height = Game.WORLDH - ter.getHeight(tex.getWidth()/2 + targetx);
		Batch.draw(tex, Cam.getRenderX(targetx), Cam.getRenderY(height));
	}
}
