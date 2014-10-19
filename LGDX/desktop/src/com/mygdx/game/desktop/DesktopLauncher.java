package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.Game;

public class DesktopLauncher 
{
	public static void main (String[] arg) 
	{
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Shoot All Teh Things";
		config.stencil = 8;
		config.backgroundFPS = -1;
		config.foregroundFPS = -1;
		
		config.width = 1366;
		config.height = 768;
		
		new LwjglApplication(new Game(config.width, config.height), config);
	}
}
