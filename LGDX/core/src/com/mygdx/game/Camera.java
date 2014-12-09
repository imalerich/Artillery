package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;

public class Camera 
{
	private Vector2 pos;
	private Vector2 worldmin;
	private Vector2 worldmax;
	
	public Camera()
	{
		pos = new Vector2();
		worldmin = new Vector2();
		worldmax = new Vector2(Float.MAX_VALUE, Float.MAX_VALUE);
	}
	
	public Camera(Vector2 Pos)
	{
		pos = Pos;
		worldmin = new Vector2();
		worldmax = new Vector2(Float.MAX_VALUE, Float.MAX_VALUE);
	}
	
	public float getRenderX(float XPos)
	{
		float xpos = XPos;
		if (pos.x > Game.WORLDW/2 && XPos < Game.SCREENW)
			xpos += Game.WORLDW;
		
		return xpos - pos.x;
	}
	
	public float getRenderY(float YPos)
	{
		return YPos - pos.y;
	}
	
	public Vector2 getPos()
	{
		return pos;
	}
	
	public void setPos(Vector2 Pos)
	{
		pos = Pos;
		validatePos();
	}
	
	public void setWorldMin(Vector2 Min)
	{
		worldmin = Min;
		validatePos();
	}
	
	public void setWorldMax(Vector2 Max)
	{
		worldmax = Max;
		validatePos();
	}
	
	public void moveHorizontal(float Dist)
	{
		pos.x += Dist;
		validatePos();
	}
	
	public void moveVertical(float Dist)
	{
		pos.y += Dist;
		validatePos();
	}
	
	private void validatePos()
	{
		if (pos.x < worldmin.x)
			pos.x = worldmax.x;
		else if (pos.x > worldmax.x)
			pos.x = worldmin.x;
		
		if (pos.y < worldmin.y)
			pos.y = worldmin.y;
		else if (pos.y > worldmax.y - Game.SCREENH)
			pos.y = worldmax.y - Game.SCREENH;
	}
}
