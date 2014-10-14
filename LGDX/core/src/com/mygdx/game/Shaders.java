package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class Shaders 
{
	public static ShaderProgram hili;
	public static ShaderProgram enemy;
	
	private static ShaderProgram current;
	private static ShaderProgram prev;
	
	public static void Release()
	{
		if (hili != null)
			hili.dispose();
		
		if (enemy != null)
			enemy.dispose();
	}
	
	public static void Init()
	{
		current = null;
		prev = null;
		
		Release();
		
		String vshader = Gdx.files.internal("shaders/def.vs").readString();
		String fshader = Gdx.files.internal("shaders/hili.fs").readString();
		String efshader = Gdx.files.internal("shaders/enemy.fs").readString();
		
		hili = new ShaderProgram(vshader, fshader);
		enemy = new ShaderProgram(vshader, efshader);
	}
	
	public static void SetShader(SpriteBatch Batch, ShaderProgram Prog)
	{
		prev = current;
		current = Prog;
		
		Batch.setShader(current);
	}
	
	public static void RevertShader(SpriteBatch Batch)
	{
		current = prev;
		prev = null;
		
		Batch.setShader(current);
	}
}
