package com.mygdx.game;

import com.badlogic.gdx.Gdx;

public class FrameRate 
{
	private static double fps = 0.0f;
	private static double msPerFrame = 0.0f;
	
	private static double timePassed = 0.0;
	private static int frames = 0;
	
	public static void Update()
	{
		timePassed += Gdx.graphics.getDeltaTime();
		frames++;
		
		// update the frame rate once per second
		if (timePassed >= 1)
		{
			if (fps > 0)
				System.out.println(fps);
			
			// calculate the frame rate and the milliseconds per frame
			fps = frames/timePassed;
			msPerFrame = 1000.0 * timePassed/frames;
			
			// reset the variables
			timePassed = 0.0;
			frames = 0;
		}
	}
	
	public static double GetFPS()
	{
		return fps;
	}
	
	public static double GetMsPerFrame()
	{
		return msPerFrame;
	}
}
