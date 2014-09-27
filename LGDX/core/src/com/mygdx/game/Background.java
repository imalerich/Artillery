package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Background {
	private Texture bg;
	private Texture stars0;
	private Texture stars1;
	
	public void Release()
	{
		bg.dispose();
		stars0.dispose();
		stars1.dispose();
	}
	
	public Background()
	{
		bg = new Texture( Gdx.files.internal("bg.png") );
		stars0 = new Texture( Gdx.files.internal("stars.png") );
		stars1 = new Texture( Gdx.files.internal("stars1.png") );
	}
	
	public void Draw(SpriteBatch Batch, int PlayerX)
	{
		Batch.draw(bg, 0, 0, Game.SCREENW, Game.SCREENH);
		
		int offset0 = (PlayerX/6) % Game.SCREENW;
		Batch.setColor(1.0f, 1.0f, 1.0f, 0.2f);
		Batch.draw(stars0, -offset0, 0, Game.SCREENW, Game.SCREENH);
		Batch.draw(stars0, Game.SCREENW-offset0, 0, Game.SCREENW, Game.SCREENH);
		
		int offset1 = (PlayerX/12) % Game.SCREENW;
		Batch.setColor(1.0f, 1.0f, 1.0f, 0.5f);
		Batch.draw(stars1, -offset1, 0, Game.SCREENW, Game.SCREENH);
		Batch.draw(stars1, Game.SCREENW-offset1, 0, Game.SCREENW, Game.SCREENH);
		
		Batch.setColor(Color.WHITE);
	}
}
