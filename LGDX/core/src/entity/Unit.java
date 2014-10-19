package entity;

import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;

public class Unit 
{
	protected Terrain ter;
	protected Vector2 pos;
	protected boolean forward;
	protected boolean moving;
	protected int speed;
	
	protected int viewRadius = 256;
	protected float barrelPhi = 0.0f;
	protected int width = 0;
	protected int height = 0;
	
	protected int mugshotIndex = 0;
	
	public void Release()
	{
		// override in implementation classes
	}
	
	public int GetMugShotIndex()
	{
		return mugshotIndex;
	}
	
	public void SetMugShotIndex(int Index)
	{
		mugshotIndex = Index;
	}
	
	public int GetViewRadius()
	{
		return viewRadius;
	}
	
	public void SetViewRadius(int Radius)
	{
		viewRadius = Radius;
	}
	
	public Vector2 GetPos()
	{
		return pos;
	}
	
	public void SetPos(Vector2 Pos)
	{
		pos = Pos;
	}
	
	public boolean IsForward()
	{
		return forward;
	}
	
	public int GetWidth()
	{
		return width;
	}
	
	public int GetHeight()
	{
		return height;
	}
	
	public int GetSpeed()
	{
		return speed;
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
		int nxtpos = (int)pos.x+frontpoint;
		if (nxtpos >= Game.WORLDW) nxtpos -= Game.WORLDW;
		if (nxtpos < 0) nxtpos += Game.WORLDW;
		int nxth = Game.WORLDH - ter.GetHeight(nxtpos) - 3;
		
		float theta = -(float)Math.atan( (nxth-pos.y)/(width/4.0f) );
		float xspeed = (float)Math.cos(theta)*tanspeed;
		
		pos.x += xspeed; 
		if (pos.x >= Game.WORLDW) pos.x -= Game.WORLDW;
		if (pos.x < 0) pos.x += Game.WORLDW;
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
		int nxtpos = (int)pos.x+backpoint;
		if (nxtpos < 0) nxtpos += Game.WORLDW;
		if (nxtpos >= Game.WORLDW) nxtpos -= Game.WORLDW;
		int nxth = Game.WORLDH - ter.GetHeight(nxtpos) - 3;
		
		float theta = -(float)Math.atan( (pos.y-nxth)/(width/4.0f) );
		float xspeed = (float)Math.cos(theta)*tanspeed;
		
		pos.x -= xspeed;
		if (pos.x < 0) pos.x += Game.WORLDW;
		if (pos.x >= Game.WORLDW) pos.x -= Game.WORLDW;
		forward = false;
		moving = true;
		
		SetHeight();
	}
	
	public void SetHeight()
	{
		// set the new height
		int nxtpos = (int)pos.x + width/2;
		if (nxtpos >= Game.WORLDW) nxtpos -= Game.WORLDW;
		if (nxtpos < 0) nxtpos += Game.WORLDW;
		
		pos.y = Game.WORLDH - ter.GetHeight(nxtpos) - 3;
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam, boolean Highlight)
	{
		// override in implementation classes
	}
}
