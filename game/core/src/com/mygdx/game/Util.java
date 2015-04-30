package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;

public class Util 
{
	public static Polygon rectToPoly(Rectangle R)
	{
		float[] vertices = new float[8];
		vertices[0] = R.x;
		vertices[1] = R.y;
		vertices[2] = R.x+R.width;
		vertices[3] = R.y;
		vertices[4] = R.x+R.width;
		vertices[5] = R.y+R.height;
		vertices[6] = R.x;
		vertices[7] = R.y+R.height;
		
		return new Polygon(vertices);
	}
	
	public static Color mergeColors(Color C0, float Percent, Color C1)
	{
		float v0 = Percent;
		float v1 = 1f - Percent;
		
		Color c = new Color(C0.r * v1, C0.g * v1, C0.b * v1, 1f);
		c.r += C1.r * v0;
		c.g += C1.g * v0;
		c.b += C1.b * v0;
		
		return c;
	}
	
	public static Color brightenColor(Color C, float Percent)
	{
		return mergeColors(C, Percent, Color.WHITE);
	}
	
	public static Color darkenColor(Color C, float Percent)
	{
		return mergeColors(C, Percent, Color.BLACK);
	}
	
	public static Color dullColor(Color C, float Percent)
	{
		Color compliment = new Color(1f - C.r, 1f - C.g, 1f - C.b, 1f);
		
		return mergeColors(C, Percent, compliment);
	}
}
