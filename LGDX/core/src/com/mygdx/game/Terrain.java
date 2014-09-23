package com.mygdx.game;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Terrain {
	// image data
	private Pixmap data;
	private Texture tex;
	private int width;
	private int height;
	private int minlevel;
	
	// terrain color
	private Color color;
	
	// clocks
	private double dropclock;
	private double holeclock;
	private int addx;
	private int addy;
	private int addr;
	
	private static final int alphamask = 0x00256;
	
	public void ResetTimers()
	{
		dropclock = 0.0;
		holeclock = 0.0;
		minlevel = 0;
		
		addx = -1;
		addy = -1;
		addr = -1;
	}
	
	public void Release()
	{
		tex.dispose();
		data.dispose();
	}
	
	public Terrain(int Width, int Height, int R, int G, int B)
	{
		// set the color
		color = new Color(R/255.0f, G/255.0f, B/255.0f, 1.0f);
		
		// set image dimensions
		width = Width;
		height = Height;
		
		// init the pixmap
		data = new Pixmap(Width, Height, Pixmap.Format.RGBA8888);
		Pixmap.setBlending(Blending.None);
		GenerateTerrain();
		
		// set initial data
		Invalidate();
		ResetTimers();
	}
	
	public Terrain(TerrainSeed Seed)
	{
		// init colors and dimensions
		color = Seed.GetColor();
		width = Seed.GetWidth();
		height = Seed.GetHeight();
		minlevel = Seed.GetMinLevel();
		
		// generate the texture representing the terrain
		data = new Pixmap(width, height, Pixmap.Format.RGBA8888);
		Pixmap.setBlending(Blending.None);
		
		// temporarily store the data that will be used to create the PixMap 
		int[] heights = new int[width];
		for (int i=0; i<width; i++)
			heights[i] = Seed.GetSeaLevel();
		
		// get peak data
		Iterator<Integer> peaks 		= Seed.GetPeaks().iterator();
		Iterator<Integer> sharpness		= Seed.GetSharpness().iterator();
		Iterator<Integer> peakwidth		= Seed.GetPeakWidth().iterator();
		Iterator<Integer> peakheight	= Seed.GetPeakHeight().iterator();
		
		// for each peak in the seed
		for (int i=0; i<Seed.GetPeakCount(); i++)
		{
			int p = peaks.next();
			int s = sharpness.next();
			int w = peakwidth.next()/2; // use the half width
			int h = peakheight.next();
			
			// add each peak
			for (int x=0; x<width; x++)
			{
				// if this point is outside the bounds of this peak
				if (x<p-w || x>p+w) continue;
				
				float add = h*(1 - (float)Math.pow( (p-x)/(float)w, 2*s ));
				heights[x] += (int)add;
			}
		}
		
		// process the roughness/softness of the terrain
		int softness = Seed.GetSoftness();
		int consistency = Seed.GetConsistencyLevel();
		
		for (int x=0; x<width; x++)
		{
			// get an offset from -1 -> 1
			float offset = x%softness;
			offset /= softness;
			offset = offset*2 - 1;
			
			heights[x] += (int)(consistency*Math.abs(offset));
		}
		
		// first, set the PixMap entirely transparent
		data.setColor(0.0f, 0.0f, 0.0f, 0.0f);
		data.fill();
		
		// construct the PixMap from the generated data
		data.setColor(color);
		for (int x=0; x<width; x++)
			for (int y=0; y<height; y++)
				if (y>height-heights[x])
					data.drawPixel(x, y);
		
		// set initial data
		Invalidate();
		ResetTimers();
	}
	
	public void SetBedrock()
	{
		// set the bottom layer to bedrock so it cannot be destroyed
		data.setColor(color);
		data.fillRectangle(0, height-minlevel, width, minlevel);
		
		// set the top layer to transparent for falling to properly work
		data.setColor(0.0f, 0.0f, 0.0f, 0.0f);
		data.fillRectangle(0, 0, width, 2);
	}
	
	public int GetHeight(int X)
	{
		int rety = 0;
		for (int y=0; y<height; y++)
			if ((data.getPixel(X, y) & alphamask) == 0)
				rety = y;
		
		return rety;
	}
	
	private void GenerateTerrain()
	{
		data.setColor(0.0f, 0.0f, 0.0f, 0.0f);
		data.fill();
		
		data.setColor(color);
		for (int x=0; x<width; x++)
			for (int y=0; y<height; y++)
				if (y>height-136) data.drawPixel(x, y);
		
		SetBedrock();
	}
	
	private void SetPixel(int X, int Y, boolean State)
	{
		if (State)
			data.setColor(color);
		else data.setColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		data.drawPixel(X, Y);
	}
	
	private void UpdateDropping()
	{
		dropclock += Gdx.graphics.getDeltaTime();
		if (dropclock < 0.01) return;
		dropclock = 0.0;
		
		// loop through each section of the terrain
		for (int x=0; x<width; x++)
		{
			int sety = height;
			boolean laststate = true;
			boolean currentstate = true;
			
			for (int y=height; y>0; y--)
			{
				// get the current state of this pixel
				if ((data.getPixel(x, y) & alphamask) == 0)
					currentstate = false;
				else currentstate = true;
				
				if (!laststate && currentstate && y != height-1)
					sety = y+1;
				else if (laststate && !currentstate && sety != height) {
					SetPixel(x, sety, true);
					SetPixel(x, y+1, false);
				}
					
				// set the last state
				laststate = currentstate;
			}
		}
	}
	
	public void Update()
	{
		UpdateDropping();
		
		holeclock += Gdx.graphics.getDeltaTime();
		if (holeclock > 1.0 && addr > 0) {
			data.setColor(0.0f, 0.0f, 0.0f, 0.0f);
			int radius = (int)(addr * 2 * (holeclock-1.0));
			
			data.fillCircle(addx, addy, radius);
		}
		
		if (holeclock > 1.5 || addr < 0) {
			holeclock = 0.0;
			addr = -1;
		}
	}
	
	public void AddHole(int X, int Y, int Radius)
	{
		addx = X;
		addy = Y;
		addr = Radius;
	}
	
	public void Invalidate()
	{
		if (tex != null)
			tex.dispose();
		
		SetBedrock();
		tex = new Texture(data);
	}
	
	public void Draw(SpriteBatch Batch, Vector2 Campos)
	{
		Batch.draw(tex, -Campos.x, -Campos.y);
	}
}