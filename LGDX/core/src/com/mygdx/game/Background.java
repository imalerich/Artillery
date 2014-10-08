package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Background {
	public static final Color BGCOLOR = new Color(60/255f, 25/255f, 22/255f, 1f);
	
	private Texture bg;
	
	public void Release()
	{
		bg.dispose();
	}
	
	public Background()
	{
		Pixmap tmp = new Pixmap(Game.SCREENW, Game.SCREENH, Pixmap.Format.RGB888);
		tmp.setColor( BGCOLOR );
		tmp.fill();
		bg = new Texture(tmp);
		tmp.dispose();
	}
	
	public void Draw(SpriteBatch Batch, int PlayerX)
	{
		Batch.draw(bg, 0, 0, Game.SCREENW, Game.SCREENH);
		
		//int offset1 = (PlayerX/3) % Game.SCREENW;
		Batch.setColor(1.0f, 1.0f, 1.0f, 0.12f);
		
		Batch.setColor(Color.WHITE);
	}
}
