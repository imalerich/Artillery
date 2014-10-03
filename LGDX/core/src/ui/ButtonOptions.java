package ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.Game;

public class ButtonOptions 
{
	public static final int MOVE = 0;
	public static final int ATTACK = 1;
	public static final int STOP = 2;
	public static final int UPGRADE = 3;
	
	private static final int BUTTONGAP = 8;
	private static final int GROWSPEED = 16;
	private static final int BUTTONDOWN = 4;
	
	private static Texture button;
	private static TextureRegion[] glyphs;
	
	private boolean buttondown;
	private boolean buttonrelease;
	
	private double clock;
	private int xpos;
	private int ypos;
	private int count;
	private int[] buttonGlyphs;
	private Rectangle[] bbox;
	
	public static void Release()
	{
		button.dispose();
	}
	
	public ButtonOptions(int XPos, int YPos, int Count)
	{
		if (button == null)
			button = new Texture( Gdx.files.internal("img/button.png") );
		
		if (glyphs == null)
		{
			Texture tmp = new Texture( Gdx.files.internal("img/glyphs.png") );
			glyphs = TextureRegion.split(tmp, button.getWidth(), button.getHeight())[0];
		}
		
		clock = 0.0;
		count = Count;
		buttondown = false;
		buttonrelease = false;
		
		buttonGlyphs = new int[count];
		for (int i=0; i<count; i++)
			buttonGlyphs[i] = 0;
		
		bbox = new Rectangle[count];
		SetPos(XPos, YPos);
	}
	
	public void ResetClock()
	{
		clock = 0.0;
	}
	
	public void SetGlyph(int ButtonIndex, int GlyphIndex)
	{
		buttonGlyphs[ButtonIndex] = GlyphIndex;
	}
	
	private void CalcBoundingBoxes()
	{
		for (int i=0; i<count; i++) 
		{
			int width = button.getWidth()*count + BUTTONGAP*(count-1);
			int x = xpos + i*button.getWidth() - width/2 + i*BUTTONGAP;
			
			
			bbox[i] = new Rectangle(x, ypos, button.getWidth(), button.getHeight());
		}
	}
	
	public void SetPos(int XPos, int YPos)
	{
		xpos = XPos;
		ypos = YPos;
		
		CalcBoundingBoxes();
	}
	
	public boolean IsButtonReleased()
	{
		return buttonrelease;
	}
	
	public int GetAction(int Button)
	{
		if (Button == -1 || Button > count)
			return -1;
		
		return buttonGlyphs[Button];
	}
	
	public int GetButtonDown()
	{
		// check if the button is pressed (but not yet released)
		if (Gdx.input.isButtonPressed(Buttons.LEFT))
			buttondown = true;
		else if (buttondown) {
			// set the button release as true and process the action
			buttonrelease = true;
			buttondown = false;
		} else if (!buttondown) {
			// the button is neither pressed nor released, do not process the action
			buttonrelease = false;
			buttondown = false;
			return -1;
		}
		
		int mousex = Gdx.input.getX();
		int mousey = Game.SCREENH - Gdx.input.getY();
		
		// loop through each button and find if the user selects it
		for (int i=0; i<count; i++)
		{
			if (bbox[i].contains(mousex, mousey))
				return i;
		}
		
		return -1;
	}
	
	public void Draw(SpriteBatch Batch)
	{
		// increment the clock
		clock += Gdx.graphics.getDeltaTime();
		float scale = 1f;
		if (GROWSPEED*clock*clock < 1.0)
			scale = GROWSPEED*(float)(clock * clock);
			
		int width = button.getWidth()*count + BUTTONGAP*(count-1);
		int selected = GetButtonDown();
		
		for (int i=0; i<count; i++)
		{
			int x = xpos + i*button.getWidth() - width/2 + i*BUTTONGAP;
			int xoff = (int)(button.getWidth()*(1-scale) )/2;
			int yoff = (int)(button.getHeight()*(1-scale) )/2;
			
			// push the button down
			if (selected == i)
				yoff -= BUTTONDOWN;
			
			Batch.draw(button, x+xoff, ypos+yoff, button.getWidth()*scale, button.getHeight()*scale);
			Batch.draw(glyphs[buttonGlyphs[i]], x+xoff, ypos+yoff, 
					button.getWidth()*scale, button.getHeight()*scale);
		}
	}
}
