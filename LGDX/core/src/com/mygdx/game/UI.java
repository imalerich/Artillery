package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class UI {
	private Texture ui;
	
	public void Release()
	{
		ui.dispose();
	}
	
	public UI(String Filename)
	{
		ui = new Texture( Gdx.files.internal(Filename) );
	}
	
	public void Draw(SpriteBatch Batch)
	{
		Batch.draw(ui, 0, 0, Game.SCREENW, Game.SCREENH);
	}
}
