package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Cursor 
{
	public static final int BUTTONCOUNT = 3;
	public static final int LEFT = 0;
	public static final int MIDDLE = 1;
	public static final int RIGHT = 2;
	
	private static final int UP = 0;
	private static final int DOWN = 1;
	private static final int PRESSED = 2;
	private static final int RELEASED = 3;
	private static int[] buttondown;
	
	public static void init()
	{
		Gdx.input.setInputProcessor( new CursorInput() );
		
		buttondown = new int[BUTTONCOUNT];
		for (int i=0; i<BUTTONCOUNT; i++)
			buttondown[i] = UP;
	}
	
	private static void setButton(int CButton, int GdxButton)
	{
		if (Gdx.input.isButtonPressed(GdxButton))
		{
			// the button state is either PRESSED or DOWN
			if (!isButtonPressed(CButton)) // if it was 'up'
				buttondown[CButton] = PRESSED; // then consider the button as 'pressed'
			else buttondown[CButton] = DOWN; // else the button is just 'down'
			
		} else {
			// the button state is either RELEASED or UP
			if (isButtonPressed(CButton)) // if the button was 'down'
				buttondown[CButton] = RELEASED; // then consider the button as 'released'
			else buttondown[CButton] = UP; //else the button is just 'up'
		}
	}
	
	public static void update()
	{
		setButton(Buttons.LEFT, LEFT);
		setButton(Buttons.MIDDLE, MIDDLE);
		setButton(Buttons.RIGHT, RIGHT);
	}
	
	public static boolean isButtonPressed(int Button)
	{
		return (buttondown[Button] == DOWN || buttondown[Button] == PRESSED);
	}
	
	public static boolean isButtonJustPressed(int Button)
	{
		return (buttondown[Button] == PRESSED);
	}
	
	public static boolean isButtonJustReleased(int Button)
	{
		return (buttondown[Button] == RELEASED);
	}
	
	public static int getScrollDirection()
	{
		return CursorInput.getScrollDirection();
	}
	
	public static int getMouseX(Vector2 Campos)
	{
		int xpos = (int)(Gdx.input.getX()*Game.SCREENRATIOX);
		if (xpos + Campos.x > Game.WORLDW)
			xpos -= Game.WORLDW;
		
		return xpos;
	}
	
	public static int getMouseY()
	{
		int ypos = (int)(Gdx.input.getY()*Game.SCREENRATIOY);
		return Game.SCREENH - ypos;
	}
	
	public static boolean isMouseOver(Rectangle R, Vector2 Campos)
	{
		if (R.contains(Campos.x + getMouseX(Campos), Campos.y + getMouseY()))
			return true;
		else if (R.contains(Campos.x + getMouseX(Campos) + Game.WORLDW, Campos.y + getMouseY()))
			return true;
		else if (R.contains(Campos.x + getMouseX(Campos) - Game.WORLDW, Campos.y + getMouseY()))
			return true;
		
		return false;
	}
	
	public static boolean isMouseOverAbsolute(Rectangle R)
	{
		int xpos = (int)(Gdx.input.getX()*Game.SCREENRATIOX);
		int ypos = (int)(Gdx.input.getY()*Game.SCREENRATIOY);
		
		if (R.contains(xpos, Game.SCREENH-ypos))
			return true;
		else if (R.contains(Gdx.input.getX()+Game.WORLDW, Game.SCREENH-Gdx.input.getY()))
			return true;
		
		return false;
	}
	
	public static boolean didMouseMove()
	{
		return (getDeltaX() != 0 || getDeltaY() != 0);
	}
	
	public static int getDeltaX()
	{
		return Gdx.input.getDeltaX();
	}
	
	public static int getDeltaY()
	{
		return Gdx.input.getDeltaY();
	}
	
}
