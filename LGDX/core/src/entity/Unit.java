package entity;

import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Game;

public class Unit 
{
	protected Terrain ter;
	protected Vector2 pos;
	protected boolean forward;
	protected boolean moving;
	protected int speed;
	protected int width = 0;
	protected int height = 0;
	
	public void Release()
	{
		// override in implementation classes
	}
	
	public Vector2 GetPos()
	{
		return pos;
	}
	
	public int GetWidth()
	{
		return width;
	}
	
	public int GetHeight()
	{
		return height;
	}
	
	public Terrain GetTerrainReference()
	{
		return ter;
	}
	
	public void SetTerrainReference(Terrain Ter)
	{
		ter = Ter;
	}
	
	public void MoveRight()
	{
		int midpoint = width/2;
		int frontpoint = midpoint + width/4;
		
		// sample the direction traveled
		float tanspeed = Gdx.graphics.getDeltaTime()*speed;
		int nexth = Game.WORLDH - ter.GetHeight((int)pos.x+frontpoint) - 3;
		float theta = -(float)Math.atan( (nexth-pos.y)/(width/4.0f) );
		float xspeed = (float)Math.cos(theta)*tanspeed;
		pos.x += xspeed; 
		forward = true;
		moving = true;
		
		SetHeight();
	}
	
	public void MoveLeft()
	{
		int midpoint = width/2;
		int backpoint = midpoint - width/4;
		
		// sample the direction traveled
		float tanspeed = Gdx.graphics.getDeltaTime()*speed;
		int nexth = Game.WORLDH - ter.GetHeight((int)pos.x+backpoint) - 3;
		float theta = -(float)Math.atan( (pos.y-nexth)/(width/4.0f) );
		float xspeed = (float)Math.cos(theta)*tanspeed;
		pos.x -= xspeed;
		forward = false;
		moving = false;
		
		SetHeight();
	}
	
	public void SetHeight()
	{
		// set the new height
		pos.y = Game.WORLDH - ter.GetHeight((int)pos.x + width/2) - 3;
	}
	
	public void Draw(SpriteBatch Batch, Vector2 Campos, boolean Highlight)
	{
		// override in implementation classes
	}
}
