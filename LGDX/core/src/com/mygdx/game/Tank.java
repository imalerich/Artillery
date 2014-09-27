package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Tank extends Entity 
{
	private Texture tex;
	private Texture barrel;
	
	private float barrelPhi; // angle of the barrel relative to the tank
	private Vector2 barrelOffset; // coordinates of the barrel relative to the tank
	
	public void Release()
	{
		tex.dispose();
		barrel.dispose();
	}
	
	public Tank(String Filename, String Barrel, Terrain Ter, int Speed)
	{
		tex = new Texture(Gdx.files.internal(Filename) );
		barrel = new Texture( Gdx.files.internal(Barrel) );
		
		pos = new Vector2();
		pos.y = Game.WORLDH - Ter.GetHeight((int)pos.x+32) - 3;
		
		width = tex.getWidth();
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
		barrelPhi += Gdx.graphics.getDeltaTime()*30;
	}
	
	public void MoveBarrelDown()
	{
		barrelPhi -= Gdx.graphics.getDeltaTime()*30;
	}
	
	public void Draw(SpriteBatch Batch, Vector2 Campos)
	{
		float theta = 0.0f;
		float h0 = ter.GetHeight((int)pos.x+16);
		float h1 = ter.GetHeight((int)pos.x+48);
		theta = -(float)Math.atan( (h1-h0)/32.0f );
		theta = (float)Math.toDegrees(theta);
		
		// draw the tanks barrel
		Vector2 offset = new Vector2(barrelOffset.x-32, barrelOffset.y);
		if (!forward) offset.x = 32 - barrelOffset.x;
		
		float x = offset.x;
		float y = offset.y;
		
		float cos = (float)Math.cos( Math.toRadians(theta));
		float sin = (float)Math.sin(Math.toRadians(theta));
		
		offset.x = x*cos - y*sin;
		offset.y = x*sin + y*cos;
		
		float phi = barrelPhi;
		if (!forward)
			phi = -phi + 180;
		
		Batch.draw(barrel, pos.x - Campos.x + 32 + offset.x, pos.y - Campos.y + offset.y, 
				0, 3, 46, 6, 1.0f, 1.0f, 
				phi + theta, 0, 0, 46, 6, false, false);
		
		// draw the tank
		Batch.draw(tex, pos.x - Campos.x, pos.y - Campos.y, 32, 0, 64, 64, 1.0f, 1.0f, 
				theta, 0, 0, 64, 64, !forward, false);
	}
}
