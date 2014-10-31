package entity;

import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;
import com.mygdx.game.Shaders;

public class Tank extends Unit 
{
	private static float MAXANGLE = 25;
	private static float MINANGLE = -15;
	
	private Texture tex;
	private static Texture barrel;
	
	private Vector2 barrelOffset; // coordinates of the barrel relative to the tank
	
	private int halfwidth;
	
	protected float barrelPhi = 0.0f;
	private int barrelwidth;
	private int barrelheight;
	
	public static void Init()
	{
		if (barrel == null) {
			barrel = new Texture( Gdx.files.internal("img/tanks/Barrel.png") );
		}
	}
	
	public static int GetBarrelWidth()
	{
		if (barrel != null) {
			return barrel.getWidth();
		} else {
			return 0;
		}
	}
	
	public void Release()
	{
		tex.dispose();
		
		if (barrel != null)
			barrel.dispose();
	}
	
	public Tank(String Filename, Terrain Ter, int Speed)
	{
		tex = new Texture(Gdx.files.internal(Filename) );
		
		halfwidth = tex.getWidth()/2;
		width = tex.getWidth();
		height = tex.getHeight();
		
		barrelwidth = barrel.getWidth();
		barrelheight = barrel.getHeight();
		
		pos = new Vector2(64, 0);
		pos.y = Game.WORLDH - Ter.GetHeight((int)pos.x+halfwidth) - 3;
		
		barrelOffset = new Vector2();
		forward = true;
		barrelPhi = 0.0f;
		ter = Ter;
		speed = Speed;
	}
	
	public Rectangle GetBBox()
	{
		// get the tanks angle
		float theta = (float)Math.toRadians( GetAngle() );
		
		// get the points describing the boundaries
		Vector2[] coords = new Vector2[4];
		coords[0] = new Vector2(-width/2f, 0f);
		coords[1] = new Vector2(-width/2f, height);
		
		coords[2]= new Vector2(width/2f, 0f);
		coords[3]= new Vector2(width/2f, height);
		
		// rotate the coordinates we are using to describe the bounding box
		for (int i=0; i<4; i++) {
			coords[i] = RotateCoord(coords[i], theta);
		}
		
		// get the min and maxes from these coordinates
		Vector2 min = new Vector2(Float.MAX_VALUE, Float.MAX_VALUE);
		Vector2 max = new Vector2(Float.MIN_VALUE, Float.MIN_VALUE);
		
		for (int i=0; i<4; i++) {
			if (coords[i].x < min.x)
				min.x = coords[i].x;
			
			if (coords[i].x > max.x)
				max.x = coords[i].x;
			
			if (coords[i].y < min.y)
				min.y = coords[i].y;
			
			if (coords[i].y > max.y)
				max.y = coords[i].y;
		}
		
		// return an axis aligned bounding box encompasing the tank
		Rectangle r = new Rectangle(min.x + pos.x + width/2f, min.y + pos.y, max.x-min.x, max.y-min.y);
		if (r.x < 0)
			r.x += Game.WORLDW;
		else if (r.x > Game.WORLDW)
			r.x -= Game.WORLDW;
		
		return r;
	}
	
	public void SetBarrelOffset(Vector2 Offset)
	{
		barrelOffset = Offset;
	}
	
	private Vector2 RotateCoord(Vector2 Coord, float Theta)
	{
		float x = Coord.x;
		float y = Coord.y;
		
		float cos = (float)Math.cos( Theta );
		float sin = (float)Math.sin( Theta );
		
		float rx = x*cos - y*sin;
		float ry = x*sin + y*cos;
		
		return new Vector2(rx, ry);
	}
	
	public float GetAngle()
	{
		float theta = 0.0f;
		int x0 = (int)pos.x + halfwidth/2;
		int x1 = (int)pos.x + halfwidth/2+halfwidth;
		
		if (x0 >= Game.WORLDW) x0 -= Game.WORLDW; 
		if (x0 < 0) x0 += Game.WORLDW;
		
		if (x1 >= Game.WORLDW) x1 -= Game.WORLDW;
		if (x1 < 0) x1 += Game.WORLDW;
		
		float h0 = ter.GetHeight(x0);
		float h1 = ter.GetHeight(x1);
		
		theta = -(float)Math.atan( (h1-h0)/(float)halfwidth );
		return (float)Math.toDegrees(theta);
	}
	
	public float GetBarrelAbsoluteAngle()
	{
		if (forward) {
			return barrelPhi + GetAngle();
		} else {
			return barrelPhi - GetAngle();
		}
	}
	
	public void SetBarrelAngle(float Angle)
	{
		if (forward) {
			barrelPhi = Angle - GetAngle();
		} else {
			barrelPhi = Angle + GetAngle();
		}
		
		// clamp the angle
		barrelPhi = Math.max(barrelPhi, MINANGLE);
		barrelPhi = Math.min(barrelPhi, MAXANGLE);
	}
	
	private void DrawOutline(SpriteBatch Batch, Camera Cam)
	{
		for (int x=-1; x<2; x++) {
			for (int y=-1; y<2; y++) {
				Render(Batch, Cam, x, y);
			}
		}
	}
	
	private void DrawTarget(SpriteBatch Batch, Camera Cam)
	{
		Shaders.SetShader(Batch, Shaders.target);
		DrawOutline(Batch, Cam);
		Shaders.RevertShader(Batch);
	}
	
	private void DrawHighlight(SpriteBatch Batch, Camera Cam)
	{
		Shaders.SetShader(Batch, Shaders.hili);
		DrawOutline(Batch, Cam);
		Shaders.RevertShader(Batch);
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam, boolean Highlight, boolean Target)
	{
		SetHeight();
		if (Highlight)
			DrawHighlight(Batch, Cam);
		else if (Target)
			DrawTarget(Batch, Cam);
		
		Render(Batch, Cam, 0, 0);
	}
	
	public void Render(SpriteBatch Batch, Camera Cam, int OffsetX, int OffsetY)
	{
		float theta = GetAngle();
		
		// draw the tanks barrel
		Vector2 offset = new Vector2(barrelOffset.x-halfwidth, barrelOffset.y);
		if (!forward) offset.x = halfwidth - barrelOffset.x;
		
		offset = RotateCoord( offset, (float)Math.toRadians(theta) );
		
		float phi = barrelPhi;
		if (!forward)
			phi = 180 - phi;
		
		// draw the tanks barrel
		Batch.draw(barrel, Cam.GetRenderX(pos.x + halfwidth + offset.x + OffsetX),
				Cam.GetRenderY(pos.y + offset.y + OffsetY),
				0, barrelheight/2f, barrelwidth, barrelheight, 1f, 1f, 
				phi + theta, 0, 0, barrelwidth, barrelheight, false, false);
		
		// draw the tank
		Batch.draw(tex, Cam.GetRenderX(pos.x + OffsetX),
				Cam.GetRenderY(pos.y + OffsetY),
				halfwidth, 0, width, height, 1f, 1f, 
				theta, 0, 0, width, height, !forward, false);
	}
	
	public void DrawTargetAngle(SpriteBatch Batch, Camera Cam)
	{
		float theta = GetAngle();
		float phi = barrelPhi;
		
		float width = Squad.target.GetFrameWidth();
		float height = Squad.target.GetFrameHeight();
		Vector2 src = new Vector2(barrelOffset.x-halfwidth, 
				barrelOffset.y);
		if (!forward)
			src.x = halfwidth - barrelOffset.x;
		src = RotateCoord( src, (float)Math.toRadians(theta));
		
		Vector2 offset = new Vector2(barrelwidth*1.8f, 0f);
		if (!forward) {
			offset.x = -barrelwidth*1.8f;
			phi = -phi;
		}
		
		offset = RotateCoord( offset, (float)Math.toRadians(phi + theta) );
		offset.x += src.x;
		offset.y += src.y;
		
		Squad.target.UpdateClock();
		Batch.draw(Squad.target.GetCurrent(0), Cam.GetRenderX(pos.x + halfwidth + offset.x - width/2f),
				Cam.GetRenderY(pos.y + offset.y - height/2f));
	}
}
