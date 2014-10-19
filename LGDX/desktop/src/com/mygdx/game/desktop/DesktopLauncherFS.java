package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.Game;

public class DesktopLauncherFS 
{
	public static void main (String[] arg) 
	{
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Shoot All Teh Things";
		int width = LwjglApplicationConfiguration.getDisplayModes()[0].width;
		int height = LwjglApplicationConfiguration.getDisplayModes()[0].height;
		
		config.stencil = 8;
		config.width = width;
		config.height = height;
		config.fullscreen = true;
		config.resizable = false;
		
		new LwjglApplication(new Game(config.width, config.height), config);
	}
}
