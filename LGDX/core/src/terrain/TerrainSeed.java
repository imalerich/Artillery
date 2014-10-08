package terrain;

import java.util.Vector;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class TerrainSeed {
	private Vector<Integer> peaks; // x location of peaks
	private Vector<Integer> sharpness;
	private Vector<Integer> peakwidth; // the width of each peak
	private Vector<Integer> peakheight; // scale of those peaks, negative = valley
	
	private Vector<Vector2> baselocation; // each vector2 is a min/max xpos for the base
	
	private Color color;
	private int width;
	private int height;
	private int sealevel;
	private int minlevel;
	private int softness;
	private int consistency;
	
	public TerrainSeed()
	{
		peaks = new Vector<Integer>();
		sharpness = new Vector<Integer>();
		peakwidth = new Vector<Integer>();
		peakheight = new Vector<Integer>();
		baselocation = new Vector<Vector2>();
		
		color = new Color();
		width = 0;
		height = 0;
		sealevel = 0;
		minlevel = 0;
		softness = 0;
		consistency = 0;
	}
	
	public void SetColor(int R, int G, int B)
	{
		color = new Color(R/255.0f, G/255.0f, B/255.0f, 1.0f);
	}
	
	public Color GetColor()
	{
		return color;
	}
	
	public void SetDimmensions(int Width, int Height)
	{
		width = Width;
		height = Height;
	}
	
	public int GetWidth()
	{
		return width;
	}
	
	public int GetHeight()
	{
		return height;
	}
	
	public void SetSoftness(int Softness)
	{
		softness = Softness;
	}
	
	public int GetSoftness()
	{
		return softness;
	}
	
	public void SetSeaLevel(int Level)
	{
		sealevel = Level;
	}
	
	public int GetSeaLevel()
	{
		return sealevel;
	}
	
	public void SetMinLevel(int Level)
	{
		minlevel = Level;
	}
	
	public int GetMinLevel()
	{
		return minlevel;
	}
	
	public void SetConsistencyLevel(int Level)
	{
		consistency = Level;
	}
	
	public int GetConsistencyLevel()
	{
		return consistency;
	}
	
	public void AddBase(int XPos, int Width)
	{
		baselocation.add( new Vector2(XPos, XPos+Width) );
	}
	
	public void AddPeak(int XPos, int Sharpness, int ScaleX, int ScaleY)
	{
		// clamp the sharpness to the bounds
		if (Sharpness < 0) Sharpness = 0;
		
		// add the data as a new peak
		peaks.add(XPos);
		sharpness.add(Sharpness);
		peakwidth.add(ScaleX);
		peakheight.add(ScaleY);
	}
	
	public int GetPeakCount()
	{
		return peaks.size();
	}
	
	public Vector<Vector2> GetBases()
	{
		return baselocation;
	}
	
	public Vector<Integer> GetPeaks()
	{
		return peaks;
	}
	
	public Vector<Integer> GetSharpness()
	{
		return sharpness;
	}
	
	public Vector<Integer> GetPeakHeight()
	{
		return peakheight;
	}
	
	public Vector<Integer> GetPeakWidth()
	{
		return peakwidth;
	}
}
