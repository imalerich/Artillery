package com.mygdx.game.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.FrameRate;
import com.mygdx.game.Game;

public class DesktopLauncher 
{
	private static boolean IsFullscreen = false;
	private static boolean IsClient = false;
	private static boolean ShowFPS = false;
	private static boolean ShowPing = false;
	private static boolean Run = true;
	
	public static void main (String[] arg) 
	{
		GetLaunchOptions(arg);
		SetGameState();
		
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.addIcon("img/icons/Icon128.png", Files.FileType.Local);
	    config.addIcon("img/icons/Icon64.png", Files.FileType.Local);
		config.addIcon("img/icons/Icon32.png", Files.FileType.Local);
		config.title = "Shoot All Teh Things";
		
		config.stencil = 8;
		config.foregroundFPS = -1;
		
		if (!IsFullscreen) {
			config.fullscreen = false;
			config.width = 1366;
			config.height = 768;
		} else {
			config.fullscreen = true;
			config.width = LwjglApplicationConfiguration.getDisplayModes()[0].width;
			config.height = LwjglApplicationConfiguration.getDisplayModes()[0].height;
		}
		
		if (Run) {
			new LwjglApplication(new Game(config.width, config.height, !IsClient), config);
		}
	}
	
	private static void SetGameState()
	{
		// do not show ping when you are the host
		if (!IsClient) 
			ShowPing = false;
		
		if (ShowFPS)
			FrameRate.SHOWFPS = true;
		
		if (ShowPing)
			FrameRate.SHOWPING = true;
	}
	
	private static void GetLaunchOptions(String[] args)
	{
		for (int i=0; i<args.length; i++) {
			String opt = args[i];
			
			if (opt.equals("?") || opt.equals("--help"))
				PrintLaunchOptions();
			else if (opt.equals("-f") || opt.equals("--fullscreen"))
				IsFullscreen = true;
			else if (opt.equals("-s") || opt.equals("--showfps"))
				ShowFPS = true;
			else if (opt.equals("-p") || opt.equals("--showping"))
				ShowPing = true;
			else if (opt.equals("-c") || opt.equals("--client"))
				IsClient = true;
			else {
				System.err.println("Error: Invalid option! Valid options are: \n");
				PrintLaunchOptions();
			}
		}
	}
	
	private static void PrintLaunchOptions()
	{
		// do not run the game, only display launch options
		Run = false;
		
		System.out.println("-----------------------------------------------------------");
		System.out.println("<?>  <--help> --- Display all launch options.");
		System.out.println("<-f> <--fullscreen> --- Run in fullscreen mode.");
		System.out.println("<-s> <--showfps> --- Show framerate counter in the console.");
		System.out.println("<-p> <--showping> --- Show the ping counter in the console.");
		System.out.println("<-c> <--client> --- Run as client, not as host.");
		System.out.println("-----------------------------------------------------------");
	}
}
