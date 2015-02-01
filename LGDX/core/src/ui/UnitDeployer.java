package ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Cursor;

public class UnitDeployer 
{
	public static final int UNITCOUNT = 3;
	private static final int GROWSPEED = 16;
	private static final int BUTTONDOWN = 4;
	
	public static final int GUNMAN = 0;
	public static final int STEALTHOPS = 1;
	public static final int SPECOPS = 2;
	
	private static TextureRegion[] glyphs;
	private static Rectangle[] bbox;
	
	private static double clock = 0.0;
	private static boolean draw = true;
	
	public static void init()
	{
		if (glyphs == null) {
			Texture tmp = new Texture( Gdx.files.internal("img/ui/indicators/units.png") );
			glyphs = TextureRegion.split(tmp, tmp.getWidth()/UNITCOUNT, tmp.getHeight())[0];
		}
		
		if (bbox == null) {
			bbox = new Rectangle[UNITCOUNT];
		}
	}
	
	public static void setDraw(boolean B)
	{
		draw = B;
	}
	
	public static void resetClock()
	{
		clock = 0.0;
	}
	
	public static void setPos(Vector2 BasePos)
	{
		for (int i=0; i<UNITCOUNT; i++) 
		{
			Vector2 pos = new Vector2(BasePos.x, BasePos.y);
			pos.x = pos.x + 130 + i*116 - glyphs[i].getRegionWidth()/2;
			pos.y = pos.y;
			
			bbox[i] = new Rectangle((int)pos.x, (int)pos.y,
					(int)glyphs[i].getRegionWidth(), (int)glyphs[i].getRegionHeight());
		}
	}
	
	public static void setBBox(Rectangle BBox, int Index)
	{
		bbox[Index] = BBox;
	}
	
	public static boolean contains(int Index)
	{
		if (Index >= 0 && Index < UNITCOUNT)
			return true;
		else
			return false;
	}
	
	public static int getSelected(Camera Cam)
	{
		for (int i=0; i<UNITCOUNT; i++)
		{
			if ( Cursor.isMouseOver(bbox[i], Cam.getPos()) )
					return i;
		}
		
		return -1;
	}
	
	public static void draw(SpriteBatch Batch, Camera Cam, int Index)
	{
		if (!draw)
			return;
		
		// increment the clock
		clock += Gdx.graphics.getDeltaTime();
		float scale = 1f;
		if (GROWSPEED*clock*clock < 1.0)
			scale = GROWSPEED*(float)(clock * clock);
			
		int selected = getSelected(Cam);

		Vector2 pos = new Vector2(bbox[Index].x, bbox[Index].y);
		pos.x += ( bbox[Index].width/2 - glyphs[Index].getRegionWidth()/2 );
		
		// offset to center after scaling
		int xoff = (int)(glyphs[Index].getRegionWidth()*(1-scale) )/2;
		int yoff = (int)(glyphs[Index].getRegionHeight()*(1-scale) )/2;

		if (selected == Index && Cursor.isButtonPressed(Cursor.LEFT))
			yoff -= BUTTONDOWN;

		pos.x = Cam.getRenderX(pos.x + xoff);
		pos.y = Cam.getRenderY(pos.y + yoff);

		Batch.draw(glyphs[Index], pos.x, pos.y, 
				glyphs[Index].getRegionWidth()*scale, glyphs[Index].getRegionHeight()*scale);
	}
}
