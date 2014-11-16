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
	
	public float GetRenderX(float XPos)
	{
		float xpos = XPos;
		if (pos.x > Game.WORLDW/2 && XPos < Game.SCREENW)
			xpos += Game.WORLDW;
		
		return xpos - pos.x;
	}
	
	public float GetRenderY(float YPos)
	{
		return YPos - pos.y;
	}
	
	public Vector2 GetPos()
	{
		return pos;
	}
	
	public void SetPos(Vector2 Pos)
	{
		pos = Pos;
		ValidatePos();
	}
	
	public void SetWorldMin(Vector2 Min)
	{
		worldmin = Min;
		ValidatePos();
	}
	
	public void SetWorldMax(Vector2 Max)
	{
		worldmax = Max;
		ValidatePos();
	}
	
	public void MoveHorizontal(float Dist)
	{
		pos.x += Dist;
		ValidatePos();
	}
	
	public void MoveVertical(float Dist)
	{
		pos.y += Dist;
		ValidatePos();
	}
	
	private void ValidatePos()
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
