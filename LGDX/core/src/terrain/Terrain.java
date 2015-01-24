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
	private static Pixmap FOXHOLE;
	
	private static final int ACCELERATION = 16;
	private static final int BASESPEED = 0;
	private static final int MAXSPEED = 4;
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
	
	// base locations (terrain underneath is invulnerable)
	private Rectangle[] baseregions;
	
	// clocks
	private double dropclock;
	
	// divide the world into segments of SEGMENTWIDTH and invalidate them when damaged
	private boolean[] tmpValid;
	private boolean[] isSegmentValid;
	private int segmentcount;
	
	private static final int SEGMENTWIDTH = 64;
	private static final int ALPHAMASK = 255;
	
	public static void init()
	{
		if (DEADTROOPR == null) {
			DEADTROOPR = new Pixmap( Gdx.files.internal("img/units/deadtroopR.png") );
		}
		
		if (DEADTROOPL == null) {
			DEADTROOPL = new Pixmap( Gdx.files.internal("img/units/deadtroopL.png") );
		}
		
		if (FOXHOLE == null) {
			FOXHOLE = new Pixmap( Gdx.files.internal("img/objects/foxhole.png") );
		}
	}
	
	public static void setColor(Color Col)
	{
		col = Col;
	}
	
	public static Color getColor()
	{
		return col;
	}
	
	public void release()
	{
		if (DEADTROOPR != null) {
			DEADTROOPR.dispose();
		}
		
		if (DEADTROOPL != null) {
			DEADTROOPL.dispose();
		}
		
		if (FOXHOLE != null) {
			FOXHOLE.dispose();
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
		baseregions = new Rectangle[Seed.getBases().size()];
		width = Seed.getWidth();
		height = Seed.getHeight();
		minlevel = Seed.getMinLevel();
		dropclock = 0.0;
		
		// generate the texture representing the terrain
		initSegments();
		Pixmap.setBlending(Blending.None);
		
		// generate the terrain
		generateFromSeed(Seed);
		
		// generate the texture
		Pixmap tmp = new Pixmap(Game.SCREENW, Game.SCREENH, Pixmap.Format.RGB888);
		tmp.setColor( Color.WHITE );
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
			invalidateSegment(i);
	}
	
	private void initSegments()
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
	
	private void generateFromSeed(TerrainSeed Seed)
	{
		// temporarily store the data that will be used to create the PixMap 
		int[] heights = new int[width];
		for (int i=0; i<width; i++)
			heights[i] = Seed.getSeaLevel();
		
		// get peak data
		Iterator<Integer> peaks 		= Seed.getPeaks().iterator();
		Iterator<Integer> sharpness		= Seed.getSharpness().iterator();
		Iterator<Integer> peakwidth		= Seed.getPeakWidth().iterator();
		Iterator<Integer> peakheight	= Seed.getPeakHeight().iterator();
		Iterator<Vector2> baselocations = Seed.getBases().iterator();
		
		// for each peak in the seed
		for (int i=0; i<Seed.getPeakCount(); i++)
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
			
			baselocations = Seed.getBases().iterator();
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
				baselocations = Seed.getBases().iterator();
				if (skip) continue;
				
				float add = h*(1 - (float)Math.pow( (p-x)/(float)w, 2*s ));
				heights[addx] += (int)add;
			}
		}
		
		// process the roughness/softness of the terrain
		int softness = Seed.getSoftness();
		int consistency = Seed.getConsistencyLevel();
		
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
			Vector2 base = Seed.getBases().get(i);
			baseregions[i] = new Rectangle(base.x, 0, base.y-base.x, getHeight((int)base.x));
		}
	}
	
	private int getSegment(int X)
	{
		return X/SEGMENTWIDTH;
	}
	
	public void setBedrock(int Segment)
	{
		// set the bottom layer to bedrock so it cannot be destroyed
		data[Segment].setColor(Color.WHITE);
		data[Segment].fillRectangle(0, height-minlevel, SEGMENTWIDTH, minlevel);
		
		for (Rectangle r : baseregions) {
			addRegion((int)r.x, (int)r.height, (int)r.width, height-(int)r.height, Segment);
			
			double theta = (float)Math.PI/2f;
			int width = (int)((r.height/Math.sin(Math.PI-theta))*Math.sin(theta));
			addTriangleRegion((int)r.x-width, width, height, (int)r.x, (int)(height-r.height), Segment);
			addTriangleRegion((int)(r.x+r.width), width, height, (int)(r.x+r.width), (int)(height-r.height), Segment);
		}
	}
	
	public int getHeight(int X)
	{
		// do not allow tests beyond the scope of the terrain
		if (X < 0) X += Game.WORLDW;
		if (X >= width) X -= Game.WORLDW;
		
		// determine the segment of this position
		int s = getSegment(X);
		int localx = X - s*SEGMENTWIDTH;
		
		// find the height
		int rety = 0;
		for (int y=0; y<height; y++)
			if ( (data[s].getPixel(localx, y) & ALPHAMASK) == 0 )
				rety = y;
		
		return rety;
	}
	
	public int getMinHeight(int X0, int X1)
	{
		if (X0 < 0)
			X0 =0;
		else if (X1 >= width)
			X1 = width-1;
		
		int min = Integer.MAX_VALUE;
		for (int x=X0; x<=X1; x++)
			min = (int)Math.min(min, getHeight(x));
		
		return min;
	}
	
	public int getMaxHeight(int X0, int X1)
	{
		if (X0 < 0)
			X0 =0;
		else if (X1 >= width)
			X1 = width-1;
		
		int max = 0;
		for (int x=X0; x<X1; x++)
			max = (int)Math.max(max, getHeight(x));
		
		return max;
	}
	
	private void setPixel(int X, int Y, boolean State)
	{
		// determine the segment for this pixel
		int s = getSegment(X);
		int localx = X - s*SEGMENTWIDTH;
		
		if (State)
			data[s].setColor(Color.WHITE);
		else data[s].setColor(Color.CLEAR);
		
		data[s].drawPixel(localx, Y);
	}
	
	private void updateDropping(int Count)
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
						setPixel(x + i*SEGMENTWIDTH, sety, true);
						setPixel(x + i*SEGMENTWIDTH, y+1, false);
						
						// set this region as invalid, continue processing
						isSegmentValid[i] = false;
					}

					// set the last state
					laststate = currentstate;
				}
			}
			
			// if this is the final iteration and the segment is invalid, invalidate it 
			if (!isSegmentValid[i] && Count == 0) {
				invalidateSegment(i);
			}
			
			// if the previous stage was invalid but it is now valid, invalidate the texture
			else if (isSegmentValid[i] && !tmpValid[i]) {
				invalidateSegment(i);
			}
			
			// copy to the tmp array
			tmpValid[i] = isSegmentValid[i];
		}
		
		// recurse down updating the terrain
		updateDropping(Count - 1);
	}
	
	public void clearTmp()
	{
		for (int i=0; i<segmentcount; i++) {
			tmpValid[i] = true;
		}
	}
	
	public void update()
	{
		dropclock += Gdx.graphics.getDeltaTime();
		if (dropclock < 0.01) return;
		
		if (isValid()) {
			speed = BASESPEED;
			return;
		} else {
			speed += Gdx.graphics.getDeltaTime()*ACCELERATION;
			if (speed > MAXSPEED) {
				speed = MAXSPEED;
			}
		}
		
		dropclock = 0.0;
		clearTmp();
		updateDropping((int)speed);
	}
	
	public void cutHole(int X, int Y, int Radius)
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
			invalidateSegment(i);
		}
	}
	
	public void cutRegion(int X, int Y, int Width, int Height)
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
			invalidateSegment(i);
		}
	}
	
	private void addTriangleRegion(int X0, int Width, int Y0, int X1, int Y1, int Segment)
	{
		if (X0 < 0) {
			X0 += Game.WORLDW;
			X1 += Game.WORLDW;
		}
		
		int localx0 = X0 - Segment*SEGMENTWIDTH;
		int localx1 = X1 - Segment*SEGMENTWIDTH;
		isSegmentValid[Segment] = false;

		data[Segment].setColor(Color.WHITE);
		data[Segment].fillTriangle(localx0, Y0, localx0+Width, Y0, localx1, Y1);
	}
	
	private void addRegion(int X, int Y, int Width, int Height, int Segment)
	{
		int localx = X - Segment*SEGMENTWIDTH;

		data[Segment].setColor(Color.WHITE);
		data[Segment].fillRectangle(localx, Y, Width, Height);
	}
	
	public void addDeceasedTroop(int X, boolean Forward)
	{
		Pixmap.setBlending(Blending.SourceOver);
		
		int width = DEADTROOPR.getWidth();
		int height = DEADTROOPR.getHeight();
		
		int x0 = X;
		int x1 = X+width;
		int y = getHeight(X + width/2)-height;
		
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
			invalidateSegment(i);
		}
		
		Pixmap.setBlending(Blending.None);
	}
	
	public void addFoxHole(int X)
	{
		Pixmap.setBlending(Blending.SourceOver);
		
		int width = FOXHOLE.getWidth();
		int height = FOXHOLE.getHeight();
		
		int x0 = X;
		int x1 = X+width;
		int y = getHeight(X + width/2)-height;
		
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
			
			data[i].drawPixmap(FOXHOLE, localx, y);
			invalidateSegment(i);
		}
		
		Pixmap.setBlending(Blending.None);
	}
	
	public boolean isValid()
	{
		for (boolean b : isSegmentValid) {
			// if a segment is not valid, return false
			if (!b)
				return false;
		}
		
		return true;
	}
	
	public void invalidateSegment(int Segment)
	{
		if (mask[Segment] != null)
			mask[Segment].dispose();
		
		setBedrock(Segment);
		mask[Segment] = new Texture( data[Segment] );
	}
	
	public boolean contains(float X, float Y)
	{
		return Y < (Game.WORLDH - getHeight((int)X));
	}
	
	public void draw(SpriteBatch Batch, Vector2 Campos)
	{
		Batch.flush();
		
		// draw the alpha mask
		Gdx.gl.glColorMask(false, false, false, true);
		Batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ZERO);
		
		// draw each segment
		int s0 = getSegment((int)Campos.x) - 1;
		s0 = (int)Math.max(s0, 0);
		int s1 = s0 + (Game.SCREENW/SEGMENTWIDTH) + 5;
		
		// render the extension
		int e0 = getSegment((int)Campos.x) - segmentcount;
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
		Batch.setColor( col );
		Batch.draw(tex, 0, 0);
		Batch.setColor( Color.WHITE );
		Batch.flush();
		
		// reset the blend function
		Batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	}
}