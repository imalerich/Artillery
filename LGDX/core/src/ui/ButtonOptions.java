package ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Cursor;

public class ButtonOptions 
{
	public static final int MOVE = 0;
	public static final int ATTACK = 1;
	public static final int STOP = 2;
	public static final int UPGRADE = 3;
	public static final int MOVEFOXHOLE = 4;
	
	private static final int BUTTONGAP = 8;
	private static final int GROWSPEED = 16;
	private static final int BUTTONDOWN = 4;
	
	private static Texture button;
	private static TextureRegion[] glyphs;
	
	private double clock;
	private int xpos;
	private int ypos;
	private int count;
	private int skipid = -1;
	private int[] buttonGlyphs;
	private Rectangle[] bbox;
	
	public static void release()
	{
		button.dispose();
	}
	
	public ButtonOptions(int XPos, int YPos, int Count)
	{
		if (button == null)
			button = new Texture( Gdx.files.internal("img/ui/indicators/button.png") );
		
		if (glyphs == null)
		{
			Texture tmp = new Texture( Gdx.files.internal("img/ui/indicators/glyphs.png") );
			glyphs = TextureRegion.split(tmp, button.getWidth(), button.getHeight())[0];
		}
		
		clock = 0.0;
		count = Count;
		
		buttonGlyphs = new int[count];
		for (int i=0; i<count; i++)
			buttonGlyphs[i] = 0;
		
		bbox = new Rectangle[count];
		setPos(XPos, YPos, new Vector2(0, 0));
	}
	
	public void noSkip()
	{
		skipid = -1;
	}
	
	public void setSkip(int ID)
	{
		skipid = ID;
	}
	
	public void resetClock()
	{
		clock = 0.0;
	}
	
	public void setGlyph(int ButtonIndex, int GlyphIndex)
	{
		buttonGlyphs[ButtonIndex] = GlyphIndex;
	}
	
	private void calcBoundingBoxes()
	{
		for (int i=0; i<count; i++) 
		{
			int width = button.getWidth()*count + BUTTONGAP*(count-1);
			int x = xpos + i*button.getWidth() - width/2 + i*BUTTONGAP;
			
			
			bbox[i] = new Rectangle(x, ypos, button.getWidth(), button.getHeight());
		}
	}
	
	public void setPos(int XPos, int YPos, Vector2 Campos)
	{
		xpos = XPos;
		ypos = YPos;
		
		calcBoundingBoxes();
	}
	
	public int getAction(int Button)
	{
		if (Button == -1 || Button > count)
			return -1;
		
		return buttonGlyphs[Button];
	}
	
	public int getButtonDown(Vector2 Campos)
	{
		int shift = 0;
		if (skipid != -1)
			shift++;
		
		// loop through each button and find if the user selects it
		for (int i=0; i<count; i++)
		{
			int mod = 0;
			if (i >= skipid && skipid != -1) {
				mod++;
				
				if (i+mod >= count)
					return -1;
			}
			
			if (Cursor.isMouseOver(bbox[i], new Vector2(Campos.x-shift*button.getWidth()/2, Campos.y)))
				return i+mod;
		}
		
		return -1;
	}
	
	public void draw(SpriteBatch Batch, Camera Cam)
	{
		// increment the clock
		clock += Gdx.graphics.getDeltaTime();
		float scale = 1f;
		if (GROWSPEED*clock*clock < 1.0)
			scale = GROWSPEED*(float)(clock * clock);
			
		int width = button.getWidth()*count + BUTTONGAP*(count-1);
		int selected = getButtonDown(Cam.getPos());
		
		for (int i=0; i<count; i++)
		{
			int x = xpos + i*button.getWidth() - width/2 + i*BUTTONGAP;
			
			if (i == skipid) {
				continue;
			} else if (i > skipid && skipid != -1) {
				x = xpos + (i-1)*button.getWidth() - width/2 + (i-1)*BUTTONGAP;
			}
			
			if (skipid != -1)
				x += button.getWidth()/2;
			
			int xoff = (int)(button.getWidth()*(1-scale) )/2;
			int yoff = (int)(button.getHeight()*(1-scale) )/2;
			
			// push the button down
			if (selected == i && Cursor.isButtonPressed(Cursor.LEFT))
				yoff -= BUTTONDOWN;
			
			Batch.draw(button, Cam.getRenderX(x+xoff), Cam.getRenderY(ypos+yoff), 
					button.getWidth()*scale, button.getHeight()*scale);
			Batch.draw(glyphs[buttonGlyphs[i]], Cam.getRenderX(x+xoff), Cam.getRenderY(ypos+yoff), 
					button.getWidth()*scale, button.getHeight()*scale);
		}
	}
}
