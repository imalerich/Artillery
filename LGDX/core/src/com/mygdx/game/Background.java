package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Background {
	private Texture bg;
	private Texture clouds;
	
	public void Release()
	{
		bg.dispose();
		clouds.dispose();
	}
	
	public Background()
	{
		bg = new Texture( Gdx.files.internal("bg.png") );
		clouds = new Texture( Gdx.files.internal("clouds.png") );
	}
	
	public void Draw(SpriteBatch Batch, int PlayerX)
	{
		Batch.draw(bg, 0, 0, Game.SCREENW, Game.SCREENH);
		
		int offset1 = (PlayerX/3) % Game.SCREENW;
		Batch.setColor(1.0f, 1.0f, 1.0f, 0.12f);
		Batch.draw(clouds, -offset1, 0, Game.SCREENW, Game.SCREENH);
		Batch.draw(clouds, Game.SCREENW-offset1, 0, Game.SCREENW, Game.SCREENH);
		
		Batch.setColor(Color.WHITE);
	}
}
