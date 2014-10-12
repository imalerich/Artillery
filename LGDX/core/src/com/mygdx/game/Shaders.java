package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class Shaders 
{
	public static ShaderProgram hili;
	
	public static void Release()
	{
		if (hili != null)
			hili.dispose();
	}
	
	public static void Init()
	{
		Release();
		
		String vshader = Gdx.files.internal("shaders/def.vs").readString();
		String fshader = Gdx.files.internal("shaders/hili.fs").readString();
		
		hili = new ShaderProgram(vshader, fshader);
	}
}
