package ui;

import java.util.ArrayList;
import java.util.Iterator;

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
	public static final int MOVE			= 0;
	public static final int MOVEFOXHOLE		= 1;
	public static final int MOVETANKTRAP	= 2;
	public static final int UPGRADE			= 3;
	public static final int TOWER			= 4;
	public static final int ATTACK			= 5;
	public static final int GRENADEL		= 6;
	public static final int GRENADER		= 7;
	public static final int STOP			= 8;
	
	private static final int BUTTONGAP = 8;
	private static final int GROWSPEED = 16;
	private static final int BUTTONDOWN = 4;
	
	private static Texture button;
	private static TextureRegion[] glyphs;
	
	private double clock;
	private int xpos;
	private int ypos;
	private ArrayList<Integer> buttonGlyphs;
	private Rectangle[] bbox;
	
	public static void release()
	{
		button.dispose();
	}
	
	public ButtonOptions(int XPos, int YPos)
	{
		if (button == null)
			button = new Texture( Gdx.files.internal("img/ui/indicators/button.png") );
		
		if (glyphs == null)
		{
			Texture tmp = new Texture( Gdx.files.internal("img/ui/indicators/glyphs.png") );
			glyphs = TextureRegion.split(tmp, button.getWidth(), button.getHeight())[0];
		}
		
		clock = 0.0;
		
		buttonGlyphs = new ArrayList<Integer>();
		setPos(XPos, YPos, new Vector2(0, 0));
	}
	
	public void resetClock()
	{
		clock = 0.0;
	}
	
	public void addGlyph(Integer Glyph)
	{
		if (buttonGlyphs.contains(Glyph)) {
			return;
		}
		
		int index = 0;
		Iterator<Integer> i = buttonGlyphs.iterator();
		while (i.hasNext()) {
			// add the glyph at the first position available
			if (Glyph < i.next()) {
				buttonGlyphs.add(index, Glyph);
				calcBoundingBoxes();
				return;
			}
			
			index++;
		}
		
		// not less than any other glyph, add to the end of the list
		calcBoundingBoxes();
		buttonGlyphs.add(Glyph);
	}
	
	public void removeGlyph(Integer Glyph)
	{
		if (!buttonGlyphs.contains(Glyph)) {
			return;
		}
		
		Iterator<Integer> i = buttonGlyphs.iterator();
		while (i.hasNext()) {
			// if the designated glyph is found, remove it
			if (i.next() == Glyph) {
				i.remove();
			}
		}
		
		calcBoundingBoxes();
	}
	
	private void calcBoundingBoxes()
	{
		bbox = new Rectangle[buttonGlyphs.size()];
		for (int i=0; i<buttonGlyphs.size(); i++) 
		{
			int width = button.getWidth()*buttonGlyphs.size()+ BUTTONGAP*(buttonGlyphs.size()-1);
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
		if (Button == -1 || Button > buttonGlyphs.size())
			return -1;
		
		return buttonGlyphs.get(Button);
	}
	
	public int getButtonDown(Vector2 Campos)
	{
		// loop through each button and find if the user selects it
		for (int i=0; i<bbox.length; i++) {
			if (Cursor.isMouseOver(bbox[i], new Vector2(Campos.x, Campos.y)))
				return i;
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
			
		int width = button.getWidth()*buttonGlyphs.size()+ BUTTONGAP*(buttonGlyphs.size()-1);
		int selected = getButtonDown(Cam.getPos());
		
		int index = 0;
		Iterator<Integer> i = buttonGlyphs.iterator();
		while (i.hasNext()) {
			Integer n = i.next();
			
			int x = xpos + index*button.getWidth() - width/2 + index*BUTTONGAP;
			
			int xoff = (int)(button.getWidth()*(1-scale) )/2;
			int yoff = (int)(button.getHeight()*(1-scale) )/2;
			
			// push the button down
			if (selected == index && Cursor.isButtonPressed(Cursor.LEFT))
				yoff -= BUTTONDOWN;
			
			Batch.draw(button, Cam.getRenderX(x+xoff), Cam.getRenderY(ypos+yoff), 
					button.getWidth()*scale, button.getHeight()*scale);
			Batch.draw(glyphs[n], Cam.getRenderX(x+xoff), Cam.getRenderY(ypos+yoff), 
					button.getWidth()*scale, button.getHeight()*scale);
			index++;
		}
	}
}
