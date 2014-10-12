package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.Game;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.stencil = 8;
		config.width = 960;
		config.height = 600;
		config.resizable = false;
		
		new LwjglApplication(new Game(config.width, config.height), config);
	}
}
