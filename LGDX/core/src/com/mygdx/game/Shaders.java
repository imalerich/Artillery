package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class Shaders 
{
	public static ShaderProgram hili;
	public static ShaderProgram target;
	public static ShaderProgram enemy;
	public static ShaderProgram health;
	
	private static ShaderProgram current;
	private static ShaderProgram prev;
	
	public static void Release()
	{
		if (hili != null)
			hili.dispose();
		
		if (enemy != null)
			enemy.dispose();
		
		if (target != null)
			target.dispose();
		
		if (health != null)
			health.dispose();
	}
	
	public static void Init()
	{
		current = null;
		prev = null;
		
		Release();
		
		String vshader = Gdx.files.internal("shaders/def.vs").readString();
		String fshader = Gdx.files.internal("shaders/hili.fs").readString();
		String tfshader = Gdx.files.internal("shaders/target.fs").readString();
		String efshader = Gdx.files.internal("shaders/enemy.fs").readString();
		String healthshader = Gdx.files.internal("shaders/health.fs").readString();
		
		hili = new ShaderProgram(vshader, fshader);
		enemy = new ShaderProgram(vshader, efshader);
		target = new ShaderProgram(vshader, tfshader);
		health = new ShaderProgram(vshader, healthshader);
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
