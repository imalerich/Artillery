package entity;

import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
		
		Coord.x = x*cos - y*sin;
		Coord.y = x*sin + y*cos;
		
		return Coord;
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
			phi = -phi + 180;
		
		// draw the tanks barrel
		Batch.draw(barrel, Cam.GetRenderX(pos.x + halfwidth + offset.x + OffsetX),
				Cam.GetRenderY(pos.y + offset.y + OffsetY),
				0, barrelheight/2, barrelwidth, barrelheight, 1.0f, 1.0f, 
				phi + theta, 0, 0, barrelwidth, barrelheight, false, false);
		
		// draw the tank
		Batch.draw(tex, Cam.GetRenderX(pos.x + OffsetX),
				Cam.GetRenderY(pos.y + OffsetY),
				halfwidth, 0, width, height, 1.0f, 1.0f, 
				theta, 0, 0, width, height, !forward, false);
	}
}
