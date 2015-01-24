package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

public class Camera 
{
	private Vector2 pos;
	private Vector2 worldmin;
	private Vector2 worldmax;
	
	private Vector2 shakepos;
	private float shakeintensity = 0f;
	private double shaketime = 0f;
	
	private Vector2 kickpos;
	private Vector2 kicktarget;
	
	private Vector2 rumblepos;
	private float rumble = 0f;
	private double rumbletime = 0f;
	
	private float xdist = 0f;
	private float ydist = 0f;
	
	public Camera()
	{
		kicktarget = new Vector2();
		kickpos = new Vector2();
		shakepos = new Vector2();
		rumblepos = new Vector2();
		
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
	
	public void addShakeIntensity(float Intensity)
	{
		shakeintensity += Intensity;
	}
	
	public void setRumble(float Intensity)
	{
		rumble = Intensity;
	}
	
	public void addKick(float X, float Y)
	{
		kicktarget.x += X;
		kicktarget.y += Y;
	}
	
	public void update()
	{
		xdist = 0f;
		ydist = 0f;
		
		// update the camera shake
		shaketime += Gdx.graphics.getDeltaTime();
		shakeintensity -= Math.pow(shaketime, 2f);
		
		if (shakeintensity < 0f) {
			shakeintensity = 0f;
			shaketime = 0f;
			
			shakepos.x = 0f;
			shakepos.y = 0f;
		}
		
		shakepos.x = (float)(shakeintensity * Math.sin(shaketime*64));
		shakepos.y = (float)(shakeintensity * Math.cos(shaketime*58));
		
		// update rumble
		rumbletime += Gdx.graphics.getDeltaTime();
		rumblepos.y = (float)(rumble * Math.cos(rumbletime*64f));
		
		// update kick back
		if (kicktarget.x == 0f || kicktarget.y == 0f)
			return;
		
		double mod = Gdx.graphics.getDeltaTime() * (kicktarget.len() - kickpos.len()) * 32f + 4f;
		
		if (kicktarget.x > 0f) {
			if (kickpos.x < kicktarget.x)
				kickpos.x += mod;
			
			if (kickpos.x > kicktarget.x)
				kickpos.x = kicktarget.x;
			
		} else if (kicktarget.x < 0f) {
			if (kickpos.x > kicktarget.x)
				kickpos.x -= mod;
			
			if (kickpos.x < kicktarget.x)
				kickpos.x = kicktarget.x;
		}
		
		if (kicktarget.y > 0f) {
			if (kickpos.y < kicktarget.y)
				kickpos.y += mod;
			
			if (kickpos.y > kicktarget.y)
				kickpos.y = kicktarget.y;
			
		} else if (kicktarget.y < 0f) {
			if (kickpos.y > kicktarget.y)
				kickpos.y -= mod;
			
			if (kickpos.y < kicktarget.y)
				kickpos.y = kicktarget.y;
		}
		
		if (kickpos.x == kicktarget.x && kickpos.y == kicktarget.y) {
			pos.x += kicktarget.x;
			pos.y += kicktarget.y;
			
			kicktarget.x = 0f;
			kicktarget.y = 0f;
			kickpos.x = 0f;
			kickpos.y = 0f;
		}
	}
	
	public float getRenderX(float XPos)
	{
		float xpos = XPos;
		if (getPos().x> Game.WORLDW/2 && XPos < Game.SCREENW)
			xpos += Game.WORLDW;
		
		return xpos - getPos().x;
	}
	
	public float getRenderY(float YPos)
	{
		return YPos - getPos().y;
	}
	
	public Vector2 getPos()
	{
		return new Vector2(pos.x + shakepos.x + kickpos.x, pos.y + shakepos.y + kickpos.y + rumblepos.y);
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
		xdist = Dist;
		pos.x += Dist;
		validatePos();
	}
	
	public void moveVertical(float Dist)
	{
		ydist = Dist;
		pos.y += Dist;
		validatePos();
	}
	
	public float getXDistMoved()
	{
		return xdist;
	}
	
	public float getYDistMoved()
	{
		return ydist;
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
