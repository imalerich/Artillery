package entity;

import java.util.Iterator;
import java.util.Vector;

import terrain.Terrain;
import ui.ButtonOptions;
import ui.PointSelect;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Cursor;
import com.mygdx.game.Game;

public class Squad 
{
	private Vector<Unit> units;
	private Rectangle bbox;
	
	private boolean menuactive;
	private boolean menurelease;
	private boolean ismoving;
	private ButtonOptions menu;
	
	private PointSelect moveselect;
	private boolean moveactive;
	private int targetpos;
	
	public Squad(Terrain Ter)
	{
		units = new Vector<Unit>();
		bbox = new Rectangle(0, 0, Float.MAX_VALUE, Float.MAX_VALUE);
		
		menuactive = false;
		menurelease = false;
		ismoving = false;
		
		menu = new ButtonOptions(0, 0, 4);
		menu.SetGlyph(0, ButtonOptions.ATTACK);
		menu.SetGlyph(1, ButtonOptions.MOVE);
		menu.SetGlyph(2, ButtonOptions.UPGRADE);
		menu.SetGlyph(3, ButtonOptions.STOP);
		
		moveselect = new PointSelect(Ter);
		moveactive = false;
		targetpos = -1;
	}
	
	private void CalcBoundingBox(Vector2 Campos)
	{
		// set to max to guarantee override
		float minx = Float.MAX_VALUE;
		float miny = Float.MAX_VALUE;
		
		float maxx = Float.MIN_VALUE;
		float maxy = Float.MIN_VALUE;
		
		float maxh = Float.MIN_VALUE;
		float maxw = Float.MIN_VALUE;
		
		Iterator<Unit> i = units.iterator();
		while (i.hasNext())
		{
			// convert he position to screen coords
			Unit n = i.next();
			Vector2 pos = new Vector2( n.GetPos() );
			
			if (pos.x < minx)
				minx = pos.x;
			if (pos.x > maxx)
				maxx = pos.x;
			
			if (pos.y < miny)
				miny = pos.y;
			if (pos.y > maxy)
				maxy = pos.y;
			
			int height = n.GetHeight();
			if (height > maxh)
				maxh = height;
			
			int width = n.GetWidth();
			if (width > maxw)
				maxw = width;
		}
		
		// construct the new bounding box
		bbox = new Rectangle(minx, miny, maxx-minx + maxw, maxy-miny + maxh);
	}
	
	private boolean IsMouseOver(Vector2 Campos)
	{
		// get the mouse position
		float mousex = Gdx.input.getX();
		float mousey = Game.SCREENH - Gdx.input.getY();
		
		// check if it is in the bounds of the bounding box
		if (bbox.contains(mousex+Campos.x, mousey+Campos.y))
			return true;
		
		return false;
	}
	
	public void AddUnit(Unit Add)
	{
		Add.SetHeight();
		units.add(Add);
	}
	
	private void UpdateMenu(Vector2 Campos)
	{
		if (Cursor.isButtonJustPressed(Cursor.RIGHT)) {
			menuactive = false;
			menurelease  = false;
			menu.ResetClock();
		}
		
		if (!Cursor.isButtonPressed(Cursor.LEFT))
			menurelease = true;
		
		int event = -1;
		if (Cursor.isButtonJustReleased(Cursor.LEFT))
			event = menu.GetAction( menu.GetButtonDown(Campos) );
			
		switch (event)
		{
		case ButtonOptions.STOP:
			// leave the menu
			menuactive = false;
			menurelease = false;
			menu.ResetClock();
			break;

		case ButtonOptions.MOVE:
			// set the movement
			moveactive = true;
			moveselect.SetPos((int)bbox.x, (int)bbox.width);
			moveselect.SetMaxDist(512);

			// leave the menu
			menuactive = false;
			menurelease = false;
			menu.ResetClock();
			break;

		default:
			break;
		}
	}
	
	private void UpdateMove(Vector2 Campos)
	{
		moveselect.Update(Campos);

		// set the target pos on left release, or cancel on right click
		if (Cursor.isButtonJustPressed(Cursor.LEFT)) {
			targetpos = moveselect.GetTargetX();
			moveactive = false;
		} else if (Cursor.isButtonJustPressed(Cursor.RIGHT))
			moveactive = false;
	}
	
	public void Update(Vector2 Campos)
	{
		if (targetpos >= 0)
			Move(Campos);
		
		if (!ismoving && IsMouseOver(Campos) && Cursor.isButtonJustPressed(Cursor.LEFT)) {
			if (menurelease) {
				menuactive = false;
				menurelease = false;
				menu.ResetClock();
			} else {
				menurelease = false;
				menuactive = true;
			}
		}
		
		if (menuactive) 
			UpdateMenu(Campos);
		
		if (moveactive)
			UpdateMove(Campos);
	}
	
	public void Move(Vector2 Campos)
	{
		if (bbox.x > targetpos)
		{
			ismoving = true;
			Iterator<Unit> i = units.iterator();
			while (i.hasNext())
				i.next().MoveLeft();
		} else if (bbox.x + bbox.width < targetpos)
		{
			ismoving = true;
			Iterator<Unit> i = units.iterator();
			while (i.hasNext())
				i.next().MoveRight();
		} else {
			ismoving = false;
		}
		
		CalcBoundingBox(Campos);
	}
	
	public void Draw(SpriteBatch Batch, Vector2 Campos)
	{
		CalcBoundingBox(Campos);
		boolean highlight = IsMouseOver(Campos) && !ismoving;
		
		if (menuactive) {
			highlight = true;
			menu.SetPos( (int)(bbox.x + bbox.width/2), (int)(bbox.y + bbox.height*1.5f), Campos );
			menu.Draw(Batch, Campos);
		}
		
		if (moveactive) {
			highlight = true;
			moveselect.Draw(Batch, Campos);
		}
		
		Iterator<Unit> i = units.iterator();
		while (i.hasNext())
			i.next().Draw(Batch, Campos, highlight);
	}
}
