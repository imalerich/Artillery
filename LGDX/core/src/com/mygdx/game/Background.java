package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Background {
	private Texture bg;
	
	public void Release()
	{
		bg.dispose();
	}
	
	public Background(String Filename)
	{
		bg = new Texture( Gdx.files.internal(Filename) );
	}
	
	public void Draw(SpriteBatch Batch)
	{
		Batch.draw(bg, 0, 0, Game.SCREENW, Game.SCREENH);
	}
}
