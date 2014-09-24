package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Tank extends Entity 
{
	private Texture tex;
	
	public void Release()
	{
		tex.dispose();
	}
	
	public Tank(String Filename, Terrain Ter)
	{
		tex = new Texture( Gdx.files.internal(Filename) );
		pos = new Vector2();
		pos.y = Game.WORLDH - Ter.GetHeight((int)pos.x+32) - 3;
		
		width = tex.getWidth();
		forward = true;
		ter = Ter;
		speed = 20;
	}
	
	public Tank(String Filename, Terrain Ter, int Speed)
	{
		tex = new Texture(Gdx.files.internal(Filename) );
		pos = new Vector2();
		pos.y = Game.WORLDH - Ter.GetHeight((int)pos.x+32) - 3;
		
		width = tex.getWidth();
		forward = true;
		ter = Ter;
		speed = Speed;
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
