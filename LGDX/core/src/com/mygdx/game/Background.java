package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Background {
	private Texture bg;
	
	public void Release()
	{
		bg.dispose();
	}
	
	public Background()
	{
		bg = new Texture( Gdx.files.internal("bg.png") );
	}
	
	public void Draw(SpriteBatch Batch, int PlayerX)
	{
		Batch.draw(bg, 0, 0, Game.SCREENW, Game.SCREENH);
		
		int offset1 = (PlayerX/3) % Game.SCREENW;
		Batch.setColor(1.0f, 1.0f, 1.0f, 0.12f);
		
		Batch.setColor(Color.WHITE);
	}
}
