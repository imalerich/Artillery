package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;

public class Camera 
{
	private Vector2 pos;
	
	public Camera()
	{
		pos = new Vector2();
	}
	
	public Camera(Vector2 Pos)
	{
		pos = Pos;
	
	}
	
	public Vector2 GetPos()
	{
		return pos;
	}
	
	public void SetPos(Vector2 Pos)
	{
		pos = Pos;
	}
}
