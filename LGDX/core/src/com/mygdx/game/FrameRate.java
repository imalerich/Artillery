package com.mygdx.game;

import network.NetworkManager;

import com.badlogic.gdx.Gdx;

public class FrameRate 
{
	public static boolean SHOWFPS = false;
	public static boolean SHOWPING = false;
	
	private static double fps = 0.0f;
	private static double msPerFrame = 0.0f;
	
	private static double timePassed = 0.0;
	private static int frames = 0;
	
	private static NetworkManager network;
	
	public static void SetNetwork(NetworkManager Network)
	{
		network = Network;
	}
	
	public static void Update()
	{
		timePassed += Gdx.graphics.getDeltaTime();
		frames++;
		
		if (SHOWPING && network != null) {
			network.UpdatePing();
		}
		
		// update the frame rate once per second
		if (timePassed >= 1)
		{
			if (fps > 0 && SHOWFPS)
				System.out.println("fps --- " + fps);
			
			// calculate the frame rate and the milliseconds per frame
			fps = frames/timePassed;
			msPerFrame = 1000.0 * timePassed/frames;
			
			// reset the variables
			timePassed = 0.0;
			frames = 0;
			
			// if the previous ping was non zero, output it
			if (network != null && SHOWPING) {
				double ping = network.GetPing();
				System.out.println("ping --- " + ping);

				// issue a ping to the host
				network.Ping();
			}
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
