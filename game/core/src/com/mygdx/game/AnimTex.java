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
	
	public void release()
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
	
	public int getFrameWidth()
	{
		return width;
	}
	
	public int getFrameHeight()
	{
		return height;
	}
	
	public void newAnimation(int Index, int FrameCount, int Start, int End, float TimeStep)
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
		time = (float)(Math.random()*TimeStep*FrameCount);
	}
	
	public void setTime(float Time)
	{
		time = Time;
	}
	
	public void updateClock()
	{
		time += Gdx.graphics.getDeltaTime();
	}
	
	public boolean isCompleted(int Index)
	{
		return animations[Index].isAnimationFinished(time-animations[Index].getFrameDuration());
	}
	
	public TextureRegion getCurrent(int Index)
	{
		return getCurrent(Index, true);
	}
	
	public TextureRegion getCurrent(int Index, boolean Looping)
	{
		currentframe = animations[Index].getKeyFrame(time, Looping);
		return currentframe;
	}
	
	public void render(SpriteBatch Batch, Camera Cam, int Index, Vector2 Pos, float XScale, float YScale)
	{
		render(Batch, Cam, Index, Pos, XScale, YScale, true);
	}
	
	public void render(SpriteBatch Batch, Camera Cam, int Index, Vector2 Pos, float XScale, float YScale, boolean Looping)
	{
		currentframe = animations[Index].getKeyFrame(time, Looping);
		
		Batch.draw(currentframe, Cam.getRenderX(Pos.x), Cam.getRenderY(Pos.y), 
				width/2.0f, height/2.0f, width, height, XScale, YScale, 0.0f);
	}
	
	public void render(SpriteBatch Batch, Camera Cam, int Index, Vector2 Pos, float XScale, float YScale, 
			boolean Looping, int SrcWidth, int SrcHeight)
	{
		currentframe = animations[Index].getKeyFrame(time, Looping);
		TextureRegion tmp = new TextureRegion(currentframe);
		tmp.setRegionY(height-SrcHeight);
		tmp.setRegionHeight(SrcHeight);
		tmp.setRegionWidth(SrcWidth);
		
		Batch.draw(tmp, Cam.getRenderX(Pos.x), Cam.getRenderY(Pos.y), 
				width/2.0f, height/2.0f, SrcWidth, SrcHeight, XScale, YScale, 0.0f);
	}
}
