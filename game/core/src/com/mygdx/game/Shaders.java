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
	public static ShaderProgram armor;
	public static ShaderProgram nulltank;
	
	private static ShaderProgram current;
	private static ShaderProgram prev;
	
	public static void release()
	{
		if (hili != null)
			hili.dispose();
		
		if (enemy != null)
			enemy.dispose();
		
		if (target != null)
			target.dispose();
		
		if (health != null)
			health.dispose();
		
		if (nulltank != null)
			nulltank.dispose();

		if (armor != null)
			armor.dispose();
	}
	
	public static void init()
	{
		current = null;
		prev = null;
		
		release();
		
		String vshader = Gdx.files.internal("shaders/def.vs").readString();
		String fshader = Gdx.files.internal("shaders/hili.fs").readString();
		String tfshader = Gdx.files.internal("shaders/target.fs").readString();
		String efshader = Gdx.files.internal("shaders/enemy.fs").readString();
		String healthshader = Gdx.files.internal("shaders/health.fs").readString();
		String armorshader = Gdx.files.internal("shaders/armor.fs").readString();
		String nullshader = Gdx.files.internal("shaders/null.fs").readString();

		hili = new ShaderProgram(vshader, fshader);
		enemy = new ShaderProgram(vshader, efshader);
		target = new ShaderProgram(vshader, tfshader);
		health = new ShaderProgram(vshader, healthshader);
		armor = new ShaderProgram(vshader, armorshader);
		nulltank = new ShaderProgram(vshader, nullshader);
	}
	
	public static void setShader(SpriteBatch Batch, ShaderProgram Prog)
	{
		prev = current;
		current = Prog;
		
		Batch.setShader(current);
	}
	
	public static void revertShader(SpriteBatch Batch)
	{
		current = prev;
		prev = null;
		
		Batch.setShader(current);
	}
}
