package entity;

import java.util.Iterator;
import java.util.Vector;

import ui.ButtonOptions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Game;

public class Squad 
{
	private Vector<Unit> units;
	private Rectangle bbox;
	
	private boolean menuactive;
	private ButtonOptions menu;
	
	public Squad()
	{
		units = new Vector<Unit>();
		bbox = new Rectangle(0, 0, Float.MAX_VALUE, Float.MAX_VALUE);
		
		menuactive = false;
		menu = new ButtonOptions(0, 0, 4);
		menu.SetGlyph(0, ButtonOptions.ATTACK);
		menu.SetGlyph(1, ButtonOptions.MOVE);
		menu.SetGlyph(2, ButtonOptions.UPGRADE);
		menu.SetGlyph(3, ButtonOptions.STOP);
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
			pos.x -= Campos.x;
			pos.y -= Campos.y;
			
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
	
	private boolean IsMouseOver()
	{
		// get the mouse position
		float mousex = Gdx.input.getX();
		float mousey = Game.SCREENH - Gdx.input.getY();
		
		// check if it is in the bounds of the bounding box
		if (bbox.contains(mousex, mousey))
			return true;
		
		return false;
	}
	
	public void AddUnit(Unit Add)
	{
		Add.SetHeight();
		units.add(Add);
	}
	
	public void Update()
	{
		if (IsMouseOver() && Gdx.input.isButtonPressed(Buttons.LEFT))
			menuactive = true;
		
		if (menuactive) 
		{
			int event = menu.GetAction( menu.GetButtonDown() );
			if (!menu.IsButtonReleased())
				event = -1;
			
			switch (event)
			{
			case ButtonOptions.STOP:
				// leave the menu
				menuactive = false;
				menu.ResetClock();
				break;
				
			default:
				break;
			}
		}
	}
	
	public void Draw(SpriteBatch Batch, Vector2 Campos)
	{
		CalcBoundingBox(Campos);
		boolean highlight = IsMouseOver();
		
		if (menuactive) {
			highlight = true;
			menu.SetPos( (int)(bbox.x + bbox.width/2), (int)(bbox.y + bbox.height*1.5f) );
			
			menu.Draw(Batch);
		}
		
		Iterator<Unit> i = units.iterator();
		while (i.hasNext())
			i.next().Draw(Batch, Campos, highlight);
	}
}
