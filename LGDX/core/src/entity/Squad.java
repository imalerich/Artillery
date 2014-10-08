package entity;

import java.util.Iterator;
import java.util.Vector;

import terrain.Terrain;
import ui.ButtonOptions;
import ui.PointSelect;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Cursor;
import com.mygdx.game.Game;

public class Squad 
{
	private static int squadspacing = 32;
	
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
		float miny = Float.MAX_VALUE;
		float maxy = Float.MIN_VALUE;
		
		float maxh = Float.MIN_VALUE;
		float maxw = Float.MIN_VALUE;
		
		Iterator<Unit> i = units.iterator();
		while (i.hasNext())
		{
			// convert he position to screen coordinates
			Unit n = i.next();
			Vector2 pos = new Vector2( n.GetPos() );
			
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
		float minx = units.firstElement().GetPos().x;
		bbox = new Rectangle(minx, miny, (units.size()-1)*squadspacing + maxw, maxy-miny + maxh);
	}
	
	private boolean IsMouseOver(Vector2 Campos)
	{
		return Cursor.IsMouseOver(bbox, Campos);
	}
	
	public void SetSquadSpacing(int SquadSpacing)
	{
		squadspacing = SquadSpacing;
	}
	
	public int GetSquadSpacing()
	{
		return squadspacing;
	}
	
	public void AddUnit(Unit Add, Camera Cam)
	{
		// get the position at which to add this unit
		Vector2 addp =  new Vector2(Add.GetPos());
		if (units.size() > 0) {
			addp = new Vector2(units.lastElement().GetPos());
			addp.x += squadspacing;
		}
		
		Add.SetPos(addp);
		Add.SetHeight();
		units.add(Add);
		
		CalcBoundingBox(Cam.GetPos());
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
			ModTarget();
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
	
	private void ModTarget()
	{
		// -1 means don't move at all
		if (targetpos < 0) return;
		
		// check the distance to the target in each direction
		float rdist = (Game.WORLDW-(bbox.x+bbox.width))+targetpos;
		if (targetpos > bbox.x+bbox.width)
			rdist = targetpos -(bbox.x+bbox.width);

		float ldist = bbox.x + (Game.WORLDW-targetpos);
		if (targetpos < bbox.x)
			ldist = (bbox.x - targetpos);
		
		// modify the target position so its facing the front of the squad
		if (rdist < ldist)
			targetpos -= (units.size()-1)*squadspacing;
		
		// set within the world bounds
		if (targetpos < 0) targetpos += Game.WORLDW;
		else if (targetpos >= Game.WORLDW) targetpos -= Game.WORLDW;
	}
	
	public void Move(Vector2 Campos)
	{
		// for each unit in this squad
		Iterator<Unit> i = units.iterator();
		int index = -1; // start at 0
		int updated = 0;
		
		// for each unit
		while (i.hasNext())
		{
			Unit u = i.next();
			index++; // start at 0
			
			Vector2 pos = u.GetPos();
			int width = u.GetWidth();
			
			int target = targetpos + index*squadspacing;
			if (target < 0) target += Game.WORLDW;
			if (target >= Game.WORLDW) target -= Game.WORLDW;
			
			// check the distance to the target in each direction
			float rdist = (Game.WORLDW-(pos.x+width)) + target;
			if (target > pos.x)
				rdist = target -(pos.x);

			float ldist = pos.x + (Game.WORLDW - target);
			if (target < pos.x)
				ldist = (pos.x - target);
			
			// check if this unit has reached his position
			if (target >= pos.x 
					&& target <= pos.x+width)
				continue;
			
			// really not good code to fix a rare problem
			if (ldist < rdist && u.IsForward() && ismoving)
				continue;
			if (rdist < ldist && !u.IsForward() && ismoving)
				continue;
			
			// move the unit
			updated++;
			if (ldist < rdist)
				u.MoveLeft();
			else u.MoveRight();
		}
		
		// if they have all met their positional conditional, stop moving them
		if (updated == 0) {
			CalcBoundingBox(Campos);
			ismoving = false;
		} else ismoving = true;
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam)
	{
		boolean highlight = IsMouseOver(Cam.GetPos()) && !ismoving;
		
		if (menuactive) {
			highlight = true;
			menu.SetPos( (int)(bbox.x + bbox.width/2), (int)(bbox.y + bbox.height*1.5f), Cam.GetPos());
			menu.Draw(Batch, Cam);
		}
		
		if (moveactive) {
			highlight = true;
			moveselect.Draw(Batch, Cam);
		}
		
		Iterator<Unit> i = units.iterator();
		while (i.hasNext())
			i.next().Draw(Batch, Cam, highlight);
	}
}
