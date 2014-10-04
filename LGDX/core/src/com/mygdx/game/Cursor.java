package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;

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
	
	public static void Init()
	{
		buttondown = new int[BUTTONCOUNT];
		for (int i=0; i<BUTTONCOUNT; i++)
			buttondown[i] = UP;
	}
	
	private static void SetButton(int CButton, int GdxButton)
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
	
	public static void Update()
	{
		SetButton(Buttons.LEFT, LEFT);
		SetButton(Buttons.MIDDLE, MIDDLE);
		SetButton(Buttons.RIGHT, RIGHT);
	}
	
	public static boolean isButtonPressed(int Button)
	{
		if (buttondown[Button] == DOWN || buttondown[Button] == PRESSED)
			return true;
		else return false;
	}
	
	public static boolean isButtonJustPressed(int Button)
	{
		if (buttondown[Button] == PRESSED)
			return true;
		else return false;
	}
	
	public static boolean isButtonJustReleased(int Button)
	{
		if (buttondown[Button] == RELEASED)
			return true;
		else return false;
	}
}
