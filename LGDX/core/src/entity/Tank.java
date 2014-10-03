package entity;

import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Game;
import com.mygdx.game.Shaders;

public class Tank extends Unit 
{
	private Texture tex;
	private Texture barrel;
	
	private float barrelPhi; // angle of the barrel relative to the tank
	private Vector2 barrelOffset; // coordinates of the barrel relative to the tank
	
	private int halfwidth;
	
	private int barrelwidth;
	private int barrelheight;
	
	private static float MAXANGLE = 25;
	private static float MINANGLE = -15;
	private static int TURNRATE = 30;
	
	public void Release()
	{
		tex.dispose();
		barrel.dispose();
	}
	
	public Tank(String Filename, String Barrel, Terrain Ter, int Speed)
	{
		tex = new Texture(Gdx.files.internal(Filename) );
		barrel = new Texture( Gdx.files.internal(Barrel) );
		
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
	
	public void MoveBarrelUp()
	{
		barrelPhi += Gdx.graphics.getDeltaTime()*TURNRATE;
		barrelPhi = Math.min(barrelPhi, MAXANGLE);
	}
	
	public void MoveBarrelDown()
	{
		barrelPhi -= Gdx.graphics.getDeltaTime()*TURNRATE;
		barrelPhi = Math.max(barrelPhi, MINANGLE);
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
	
	private float GetAngle()
	{
		float theta = 0.0f;
		float h0 = ter.GetHeight( (int)pos.x + halfwidth/2 );
		float h1 = ter.GetHeight( (int)pos.x + halfwidth/2+halfwidth );
		
		theta = -(float)Math.atan( (h1-h0)/(float)halfwidth );
		return (float)Math.toDegrees(theta);
	}
	
	public void DrawHighlight(SpriteBatch Batch, Vector2 Campos)
	{
		// draw a highlighted version of the sprite
		Batch.setShader(Shaders.hili);
		
		for (int x=-1; x<2; x++) {
			for (int y=-1; y<2; y++) {
				Render(Batch, Campos, x, y);
			}
		}
		
		Batch.setShader(null);
	}
	
	public void Draw(SpriteBatch Batch, Vector2 Campos, boolean Highlight)
	{
		if (Highlight)
			DrawHighlight(Batch, Campos);
		Render(Batch, Campos, 0, 0);
	}
	
	public void Render(SpriteBatch Batch, Vector2 Campos, int OffsetX, int OffsetY)
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
		Batch.draw(barrel, pos.x - Campos.x + halfwidth + offset.x + OffsetX, 
				pos.y - Campos.y + offset.y + OffsetY, 
				0, barrelheight/2, barrelwidth, barrelheight, 1.0f, 1.0f, 
				phi + theta, 0, 0, barrelwidth, barrelheight, false, false);
		
		// draw the tank
		Batch.draw(tex, pos.x - Campos.x + OffsetX, pos.y - Campos.y + OffsetY, 
				halfwidth, 0, width, height, 1.0f, 1.0f, 
				theta, 0, 0, width, height, !forward, false);
	}
}
