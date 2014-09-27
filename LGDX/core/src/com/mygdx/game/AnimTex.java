package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class AnimTex 
{
	private float time;
	private int rows;
	private int columns;
	private int width;
	private int height;
	
	private Texture spritesheet;
	private Animation[] animations;
	private TextureRegion currentframe;
	
	public void Release()
	{
		if (spritesheet != null)
			spritesheet.dispose();
	}
	
	public AnimTex(String Spritesheet, int Rows, int Columns, int AnimCount)
	{
		// load assets
		spritesheet = new Texture(Gdx.files.internal(Spritesheet));
		animations = new Animation[AnimCount];
		
		// set data
		rows = Rows;
		columns = Columns;
		width = spritesheet.getWidth()/columns;
		height = spritesheet.getHeight()/rows;
	}
	
	public AnimTex(Texture Spritesheet, int Rows, int Columns, int AnimCount)
	{
		// load assets
		spritesheet = Spritesheet;
		animations = new Animation[AnimCount];
		
		// set data
		rows = Rows;
		columns = Columns;
		width = spritesheet.getWidth()/columns;
		height = spritesheet.getHeight()/rows;
	}
	
	public int GetFrameWidth()
	{
		return width;
	}
	
	public int GetFrameHeight()
	{
		return height;
	}
	
	public void NewAnimation(int Index, int FrameCount, int Start, int End, float TimeStep)
	{
		TextureRegion[] frames = new TextureRegion[FrameCount];
		TextureRegion[][] tmp = TextureRegion.split(spritesheet, width, height);
		
		int k=0;
		for (int i=Start; i<End+1; i++)
		{
			int row = i/columns;
			frames[k++] = tmp[row][i - row*columns];
		}
		
		animations[Index] = new Animation(TimeStep, frames);
		time = 0.0f;
	}
	
	public void Render(SpriteBatch Batch, int Index, Vector2 Pos, float XScale, float YScale)
	{
		time += Gdx.graphics.getDeltaTime();
		currentframe = animations[Index].getKeyFrame(time, true);
		
		Batch.draw(currentframe, Pos.x, Pos.y, width/2.0f, height/2.0f, width, height, XScale, YScale, 0.0f);
	}
}
