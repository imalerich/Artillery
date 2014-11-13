package terrain;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Game;

public class Terrain 
{
	private static Pixmap DEADTROOPR;
	private static Pixmap DEADTROOPL;
	
	private static final int ACCELERATION = 16;
	private static final int BASESPEED = 1;
	private static final int MAXSPEED = 2;
	private float speed = BASESPEED;
	
	// image data
	private static Color col;
	private Pixmap[] data;
	private Texture[] mask;
	
	private Texture tex;
	private Texture clreg;
	
	private int width;
	private int height;
	private int minlevel;
	
	// base locatiosn (terrain underneath is invulnerable)
	private Rectangle[] baseregions;
	
	// clocks
	private double dropclock;
	
	// divide the world into segments of SEGMENTWIDTH and invalidate them when damaged
	private boolean[] tmpValid;
	private boolean[] isSegmentValid;
	private int segmentcount;
	
	private static final int SEGMENTWIDTH = 64;
	private static final int ALPHAMASK = 255;
	
	public static void Init()
	{
		if (DEADTROOPR == null) {
			DEADTROOPR = new Pixmap( Gdx.files.internal("img/units/deadtroopR.png") );
		}
		
		if (DEADTROOPL == null) {
			DEADTROOPL = new Pixmap( Gdx.files.internal("img/units/deadtroopL.png") );
		}
	}
	
	public static void SetColor(Color Col)
	{
		col = Col;
	}
	
	public static Color GetColor()
	{
		return col;
	}
	
	public void Release()
	{
		if (DEADTROOPR != null) {
			DEADTROOPR.dispose();
		}
		
		if (DEADTROOPL != null) {
			DEADTROOPL.dispose();
		}
		
		for (int i=0; i<segmentcount; i++) {
			mask[i].dispose();
			data[i].dispose();
		}
		
		tex.dispose();
		clreg.dispose();
	}
	
	public Terrain(TerrainSeed Seed)
	{
		// init colors and dimensions
		baseregions = new Rectangle[Seed.GetBases().size()];
		width = Seed.GetWidth();
		height = Seed.GetHeight();
		minlevel = Seed.GetMinLevel();
		dropclock = 0.0;
		
		// generate the texture representing the terrain
		InitSegments();
		Pixmap.setBlending(Blending.None);
		
		// generate the terrain
		GenerateFromSeed(Seed);
		
		// generate the texture
		Pixmap tmp = new Pixmap(Game.SCREENW, Game.SCREENH, Pixmap.Format.RGB888);
		tmp.setColor( col );
		tmp.fill();
		tex = new Texture(tmp);
		tmp.dispose();
		
		// generate a clear region to hide the top of the map
		tmp = new Pixmap(Game.SCREENW, Game.SCREENH, Pixmap.Format.Alpha);
		tmp.setColor( Color.CLEAR );
		tmp.fill();
		clreg = new Texture(tmp);
		tmp.dispose();
		
		for (int i=0; i<segmentcount; i++)
			InvalidateSegment(i);
	}
	
	private void InitSegments()
	{
		// generate the validation segments
		segmentcount = (int)(Math.ceil( (float)width/SEGMENTWIDTH) );
		isSegmentValid = new boolean[segmentcount];
		tmpValid = new boolean[segmentcount];
		for (int i=0; i<segmentcount; i++) {
			tmpValid[i] = true;
			isSegmentValid[i] = false;
		}
		
		// generate the data buffers
		data = new Pixmap[segmentcount];
		mask = new Texture[segmentcount];
		
		for (int i=0; i<segmentcount; i++) {
			data[i] = new Pixmap(SEGMENTWIDTH, height, Pixmap.Format.Alpha);
			
			// init to transparent
			data[i].setColor(Color.CLEAR);
			data[i].fill();
		}
	}
	
	private void GenerateFromSeed(TerrainSeed Seed)
	{
		// temporarily store the data that will be used to create the PixMap 
		int[] heights = new int[width];
		for (int i=0; i<width; i++)
			heights[i] = Seed.GetSeaLevel();
		
		// get peak data
		Iterator<Integer> peaks 		= Seed.GetPeaks().iterator();
		Iterator<Integer> sharpness		= Seed.GetSharpness().iterator();
		Iterator<Integer> peakwidth		= Seed.GetPeakWidth().iterator();
		Iterator<Integer> peakheight	= Seed.GetPeakHeight().iterator();
		Iterator<Vector2> baselocations = Seed.GetBases().iterator();
		
		// for each peak in the seed
		for (int i=0; i<Seed.GetPeakCount(); i++)
		{
			int p = peaks.next();
			int s = sharpness.next();
			int w = peakwidth.next()/2; // use the half width
			int h = peakheight.next();

			// skip this peak if it would overlap a base
			boolean skip = false;
			while (baselocations.hasNext()) {
				Vector2 b = baselocations.next();
				if (p-w <= b.x && p+w >= b.x)
					skip = true;
				if (p-w >= b.x && p-w <= b.y)
					skip = true;
				
				if (p-w <= b.x+width && p+w >= b.x+width)
					skip = true;
				if (p-w >= b.x+width && p-w <= b.y+width)
					skip = true;
			}
			
			baselocations = Seed.GetBases().iterator();
			if (skip) continue;
				
			// add the peak
			for (int x=-width; x<width*2-2; x++)
			{
				// if this point is outside the bounds of this peak
				if (x<p-w || x>p+w) continue;
				
				// wrap peaks around the terrain, so it is uniform at the seam
				int addx = x;
				if (x < 0) addx+=width;
				else if (x >= width) addx -= width;
			
				// reset the iterator and check whether or not we should continue
				baselocations = Seed.GetBases().iterator();
				if (skip) continue;
				
				float add = h*(1 - (float)Math.pow( (p-x)/(float)w, 2*s ));
				heights[addx] += (int)add;
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
		
		// construct the PixMap from the generated data
		for (int i=0; i<segmentcount; i++)
		{
			data[i].setColor(Color.WHITE);
			
			// fill the segments with the terrain data
			for (int x=0; x<SEGMENTWIDTH; x++)
				for (int y=0; y<height; y++)
					if (y > height-heights[x + i*SEGMENTWIDTH])
						data[i].drawPixel(x, y);
			
			// leave a transparent line at the top of the pixmap
			data[i].setColor(0.0f, 0.0f, 0.0f, 0.0f);
			data[i].fillRectangle(0, 0, SEGMENTWIDTH, 2);
		}
		
		// construct the base positions
		for (int i=0; i<baseregions.length; i++) {
			Vector2 base = Seed.GetBases().get(i);
			baseregions[i] = new Rectangle(base.x, 0, base.y-base.x, GetHeight((int)base.x));
		}
	}
	
	private int GetSegment(int X)
	{
		return X/SEGMENTWIDTH;
	}
	
	public void SetBedrock(int Segment)
	{
		// set the bottom layer to bedrock so it cannot be destroyed
		data[Segment].setColor(Color.WHITE);
		data[Segment].fillRectangle(0, height-minlevel, SEGMENTWIDTH, minlevel);
		
		for (Rectangle r : baseregions) {
			AddRegion((int)r.x, (int)r.height, (int)r.width, height-(int)r.height);
		}
	}
	
	public int GetHeight(int X)
	{
		// do not allow tests beyond the scope of the terrain
		if (X < 0) X += Game.WORLDW;
		if (X >= width) X -= Game.WORLDW;
		
		// determine the segment of this position
		int s = GetSegment(X);
		int localx = X - s*SEGMENTWIDTH;
		
		// find the height
		int rety = 0;
		for (int y=0; y<height; y++)
			if ( (data[s].getPixel(localx, y) & ALPHAMASK) == 0 )
				rety = y;
		
		return rety;
	}
	
	public int GetMinHeight(int X0, int X1)
	{
		int min = Integer.MAX_VALUE;
		for (int x=X0; x<=X1; x++)
			min = (int)Math.min(min, GetHeight(x));
		
		return min;
	}
	
	public int GetMaxHeight(int X0, int X1)
	{
		int max = 0;
		for (int x=X0; x<X1; x++)
			max = (int)Math.max(max, GetHeight(x));
		
		return max;
	}
	
	private void SetPixel(int X, int Y, boolean State)
	{
		// determine the segment for this pixel
		int s = GetSegment(X);
		int localx = X - s*SEGMENTWIDTH;
		
		if (State)
			data[s].setColor(Color.WHITE);
		else data[s].setColor(Color.CLEAR);
		
		data[s].drawPixel(localx, Y);
	}
	
	private void UpdateDropping(int Count)
	{
		if (Count == -1) {
			return;
		}
		
		// declare the variables only once for the loop
		int sety = height;
		boolean laststate = true;
		boolean currentstate = true;
		
		// loop through each invalid section of the terrain
		for (int i=0; i<segmentcount; i++)
		{
			// only update invalid segments
			if (isSegmentValid[i])
				continue;
			
			// assume this region is valid until specified as otherwise
			isSegmentValid[i] = true;
			
			for (int x=0; x<SEGMENTWIDTH; x++)
			{
				sety = height;
				laststate = true;
				currentstate = true;

				for (int y=height; y>0; y--)
				{
					// get the current state of this pixel
					if ((data[i].getPixel(x, y) & ALPHAMASK) == 0)
						currentstate = false;
					else currentstate = true;

					if (!laststate && currentstate && y != height-1)
						sety = y+1;
					else if (laststate && !currentstate && sety != height) {
						SetPixel(x + i*SEGMENTWIDTH, sety, true);
						SetPixel(x + i*SEGMENTWIDTH, y+1, false);
						
						// set this region as invalid, continue processing
						isSegmentValid[i] = false;
					}

					// set the last state
					laststate = currentstate;
				}
			}
			
			// if this is the final iteration and the segment is invalid, invalidate it 
			if (!isSegmentValid[i] && Count == 0) {
				InvalidateSegment(i);
			}
			
			// if the previous stage was invalid but it is now valid, invalidate the texture
			else if (isSegmentValid[i] && !tmpValid[i]) {
				InvalidateSegment(i);
			}
			
			// copy to the tmp array
			tmpValid[i] = isSegmentValid[i];
		}
		
		// recurse down updating the terrain
		UpdateDropping(Count - 1);
	}
	
	public void ClearTmp()
	{
		for (int i=0; i<segmentcount; i++) {
			tmpValid[i] = true;
		}
	}
	
	public void Update()
	{
		dropclock += Gdx.graphics.getDeltaTime();
		if (dropclock < 0.01) return;
		
		if (IsValid()) {
			speed = BASESPEED;
			return;
		} else {
			speed += Gdx.graphics.getDeltaTime()*ACCELERATION;
			if (speed > MAXSPEED) {
				speed = MAXSPEED;
			}
		}
		
		dropclock = 0.0;
		ClearTmp();
		UpdateDropping((int)speed);
	}
	
	public void CutHole(int X, int Y, int Radius)
	{
		// determine which segments this hole belongs to
		int x0 = X - Radius;
		int x1 = X + Radius;
		
		int region0 = (int)(Math.floor( (float)x0/SEGMENTWIDTH) );
		region0 = Math.max(region0, 0);
		int region1 = (int)(Math.floor( (float)x1/SEGMENTWIDTH) );
		region1 = Math.min(region1, segmentcount-1);
		
		// for each segment, invalidate it, and cut the hole
		for (int i=region0; i<=region1; i++)
		{
			int localx = X - i*SEGMENTWIDTH;
			isSegmentValid[i] = false;
			
			data[i].setColor(Color.CLEAR);
			data[i].fillCircle(localx, Y, Radius);
			InvalidateSegment(i);
		}
	}
	
	public void CutRegion(int X, int Y, int Width, int Height)
	{
		// determine the segments this hole belongs to
		int x0 = X;
		int x1 = X + Width;
		
		int region0 = (int)(Math.floor( (float)x0/SEGMENTWIDTH) );
		region0 = Math.max(region0, 0);
		int region1 = (int)(Math.floor( (float)x1/SEGMENTWIDTH) );
		region1 = Math.min(region1, segmentcount-1);
		
		// for each segment, invalidate it, and cut the region
		for (int i=region0; i<=region1; i++)
		{
			int localx = X - i*SEGMENTWIDTH;
			isSegmentValid[i] = false;
			
			data[i].setColor(Color.CLEAR);
			data[i].fillRectangle(localx, Y, Width, Height);
			InvalidateSegment(i);
		}
	}
	
	private void AddRegion(int X, int Y, int Width, int Height)
	{
		// determine the segments this hole belongs to
		int x0 = X;
		int x1 = X + Width;
		
		int region0 = (int)(Math.floor( (float)x0/SEGMENTWIDTH) );
		region0 = Math.max(region0, 0);
		int region1 = (int)(Math.floor( (float)x1/SEGMENTWIDTH) );
		region1 = Math.min(region1, segmentcount-1);
		
		// for each segment, invalidate it, and cut the region
		for (int i=region0; i<=region1; i++)
		{
			int localx = X - i*SEGMENTWIDTH;
			isSegmentValid[i] = false;
			
			data[i].setColor(Color.WHITE);
			data[i].fillRectangle(localx, Y, Width, Height);
		}
	}
	
	public void AddDeceasedTroop(int X, boolean Forward)
	{
		Pixmap.setBlending(Blending.SourceOver);
		
		int width = DEADTROOPR.getWidth();
		int height = DEADTROOPR.getHeight();
		
		int x0 = X;
		int x1 = X+width;
		int y = GetHeight(X + width/2)-height;
		
		int region0 = (int)(Math.floor( (float)x0/SEGMENTWIDTH) );
		region0 = Math.max(region0, 0);
		int region1 = (int)(Math.floor( (float)x1/SEGMENTWIDTH) );
		region1 = Math.min(region1, segmentcount-1);
		
		for (int i=region0; i<=region1; i++)
		{
			int localx = X - i*SEGMENTWIDTH;
			if (localx+width < 0 || localx > SEGMENTWIDTH) {
				continue;
			}
			
			isSegmentValid[i] = false;
			
			if (Forward)
				data[i].drawPixmap(DEADTROOPR, localx, y);
			else
				data[i].drawPixmap(DEADTROOPL, localx, y);
			InvalidateSegment(i);
		}
		
		Pixmap.setBlending(Blending.None);
	}
	
	public boolean IsValid()
	{
		for (boolean b : isSegmentValid) {
			// if a segment is not valid, return false
			if (!b)
				return false;
		}
		
		return true;
	}
	
	public void InvalidateSegment(int Segment)
	{
		if (mask[Segment] != null)
			mask[Segment].dispose();
		
		SetBedrock(Segment);
		mask[Segment] = new Texture( data[Segment] );
	}
	
	public boolean Contains(float X, float Y)
	{
		return Y < (Game.WORLDH - GetHeight((int)X));
	}
	
	public void Draw(SpriteBatch Batch, Vector2 Campos)
	{
		Batch.flush();
		
		// draw the alpha mask
		Gdx.gl.glColorMask(false, false, false, true);
		Batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ZERO);
		
		// draw each segment
		int s0 = GetSegment((int)Campos.x) - 1;
		s0 = (int)Math.max(s0, 0);
		int s1 = s0 + (Game.SCREENW/SEGMENTWIDTH) + 5;
		
		// render the extension
		int e0 = GetSegment((int)Campos.x) - segmentcount;
		e0 = (int)Math.max(e0, 0);
		int e1 = e0 + (Game.SCREENW/SEGMENTWIDTH) + 5;
		e1 = Math.min(e1, mask.length-1);
		
		if (s1 > segmentcount)
			for (int i=e0; i<e1; i++)
				Batch.draw(mask[i], width + (i*SEGMENTWIDTH)-Campos.x, -Campos.y);
		
		// render the primary map
		for (int i=s0; i<(int)Math.min(s1, segmentcount); i++)
			Batch.draw(mask[i], (i*SEGMENTWIDTH)-Campos.x, -Campos.y);
		
		// draw a clear region above the bounds of the mask
		if (height-Campos.y < Game.SCREENH);
			Batch.draw(clreg, 0, height-Campos.y);
		
		Batch.flush();
		
		// render the texture
		Gdx.gl.glColorMask(true, true, true, true);
		Batch.setBlendFunction(GL20.GL_DST_ALPHA, GL20.GL_ONE_MINUS_DST_ALPHA);
		Batch.draw(tex, 0, 0);
		Batch.flush();
		
		// reset the blend function
		Batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	}
}