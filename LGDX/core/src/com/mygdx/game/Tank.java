package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Tank {
	private Terrain ter;
	private Texture tex;
	private Vector2 pos;
	private boolean forward;
	private int speed;
	
	public void Release()
	{
		ter = null;
		tex.dispose();
	}
	
	public Tank(String Filename, Terrain Ter)
	{
		tex = new Texture( Gdx.files.internal(Filename) );
		pos = new Vector2();
		pos.y = Game.SCREENH - Ter.GetHeight((int)pos.x);
		
		forward = true;
		ter = Ter;
		speed = 20;
	}
	
	public Tank(String Filename, Terrain Ter, int Speed)
	{
		tex = new Texture(Gdx.files.internal(Filename) );
		pos = new Vector2();
		forward = true;
		ter = Ter;
		speed = Speed;
	}
	
	public Vector2 GetPos()
	{
		return pos;
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
		// sample the direction traveled
		float tanspeed = Gdx.graphics.getDeltaTime()*speed;
		int nexth = Game.SCREENH - ter.GetHeight((int)pos.x+48) - 3;
		float theta = -(float)Math.atan( (nexth-pos.y)/16.0f );
		float xspeed = (float)Math.cos(theta)*tanspeed;
		pos.x += xspeed; 
		forward = true;
			
		// set the new height
		pos.y = Game.SCREENH - ter.GetHeight((int)pos.x+32) - 3;
	}
	
	public void MoveLeft()
	{
		// sample the direction traveled
		float tanspeed = Gdx.graphics.getDeltaTime()*speed;
		int nexth = Game.SCREENH - ter.GetHeight((int)pos.x+16) - 3;
		float theta = -(float)Math.atan( (pos.y-nexth)/16.0f );
		float xspeed = (float)Math.cos(theta)*tanspeed;
		pos.x -= xspeed;
		forward = false;
		
		// set the new height
		pos.y = Game.SCREENH - ter.GetHeight((int)pos.x+32) - 3;
	}
	
	public void Draw(SpriteBatch Batch, Vector2 Campos)
	{
		float theta = 0.0f;
		float h0 = ter.GetHeight((int)pos.x+16);
		float h1 = ter.GetHeight((int)pos.x+48);
		theta = -(float)Math.atan( (h1-h0)/32.0f );
		theta = (float)Math.toDegrees(theta);
		
		// draw the tank
		Batch.draw(tex, pos.x - Campos.x, pos.y - Campos.y, 32, 0, 64, 64, 1.0f, 1.0f, 
				theta, 0, 0, 64, 64, !forward, false);
	}
}
