package terrain;

import java.util.Vector;

import com.badlogic.gdx.math.Vector2;

public class TerrainSeed {
	private Vector<Integer> peaks; // x location of peaks
	private Vector<Integer> sharpness;
	private Vector<Integer> peakwidth; // the width of each peak
	private Vector<Integer> peakheight; // scale of those peaks, negative = valley
	
	private Vector<Vector2> baselocation; // each vector2 is a min/max xpos for the base
	
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
		
		width = 0;
		height = 0;
		sealevel = 0;
		minlevel = 0;
		softness = 0;
		consistency = 0;
	}
	
	public void setDimmensions(int Width, int Height)
	{
		width = Width;
		height = Height;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public void setSoftness(int Softness)
	{
		softness = Softness;
	}
	
	public int getSoftness()
	{
		return softness;
	}
	
	public void setSeaLevel(int Level)
	{
		sealevel = Level;
	}
	
	public int getSeaLevel()
	{
		return sealevel;
	}
	
	public void setMinLevel(int Level)
	{
		minlevel = Level;
	}
	
	public int getMinLevel()
	{
		return minlevel;
	}
	
	public void setConsistencyLevel(int Level)
	{
		consistency = Level;
	}
	
	public int getConsistencyLevel()
	{
		return consistency;
	}
	
	public void addBase(int XPos, int Width)
	{
		baselocation.add( new Vector2(XPos, XPos+Width) );
	}
	
	public void addPeak(int XPos, int Sharpness, int ScaleX, int ScaleY)
	{
		// clamp the sharpness to the bounds
		if (Sharpness < 0) Sharpness = 0;
		
		// add the data as a new peak
		peaks.add(XPos);
		sharpness.add(Sharpness);
		peakwidth.add(ScaleX);
		peakheight.add(ScaleY);
	}
	
	public int getPeakCount()
	{
		return peaks.size();
	}
	
	public Vector<Vector2> getBases()
	{
		return baselocation;
	}
	
	public Vector<Integer> getPeaks()
	{
		return peaks;
	}
	
	public Vector<Integer> getSharpness()
	{
		return sharpness;
	}
	
	public Vector<Integer> getPeakHeight()
	{
		return peakheight;
	}
	
	public Vector<Integer> getPeakWidth()
	{
		return peakwidth;
	}
}
