package com.mygdx.game;

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
}
