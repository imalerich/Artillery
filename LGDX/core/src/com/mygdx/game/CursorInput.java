package com.mygdx.game;

import com.badlogic.gdx.InputProcessor;

public class CursorInput implements InputProcessor
{
	private static int scrollDirection = 0;
	
	public static int GetScrollDirection()
	{
		return scrollDirection;
	}
	
	public static void ClearInput()
	{
		scrollDirection = 0;
	}
	
	@Override
	public boolean keyDown(int keycode)
	{
		return false;
	}

	@Override
	public boolean keyUp(int keycode)
	{
		return false;
	}

	@Override
	public boolean keyTyped(char character)
	{
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button)
	{
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button)
	{
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer)
	{
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY)
	{
		return false;
	}

	@Override
	public boolean scrolled(int amount)
	{
		scrollDirection = amount;
		
		return false;
	}
}
