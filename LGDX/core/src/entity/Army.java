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
import com.mygdx.game.MilitaryBase;

public class Army 
{
	MilitaryBase base;
	Vector<Squad> squads;
	
	private boolean menuactive;
	private boolean menurelease;
	private ButtonOptions menu;
	
	private PointSelect moveselect;
	private boolean moveactive;
	
	private Squad selected; // the currently selected squad, or null
	private boolean isUserControlled;
	
	public Army(MilitaryBase Base, Terrain Ter)
	{
		base = Base;
		squads = new Vector<Squad>();
		
		menuactive = false;
		menurelease = false;
		
		menu = new ButtonOptions(0, 0, 4);
		menu.SetGlyph(0, ButtonOptions.ATTACK);
		menu.SetGlyph(1, ButtonOptions.MOVE);
		menu.SetGlyph(2, ButtonOptions.UPGRADE);
		menu.SetGlyph(3, ButtonOptions.STOP);
		
		moveselect = new PointSelect(Ter);
		moveactive = false;
		isUserControlled = false;
		selected = null;
	}
	
	public void SetUserControlled(boolean IsControlled)
	{
		isUserControlled = IsControlled;
	}
	
	public void AddSquad(Squad Add)
	{
		squads.add(Add);
	}
	
	public void Update(Camera Cam)
	{
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext())
			s.next().Update(Cam.GetPos());
		
		if (isUserControlled) {
			SetSelectedSquad(Cam.GetPos());
			UpdateMenu(Cam);
		}
	}
	
	private void SetSelectedSquad(Vector2 Campos)
	{
		if (menuactive || moveactive) 
			return;
		
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			Squad c = s.next();
			
			if (c.IsMouseOver(Campos)) {
				
				// set the first found squad with the mouse over
				selected = c;
				return;
			}
		}
		
		selected = null;
	}
	
	private void UpdateMenu(Camera Cam)
	{
		if (selected == null)
			return;
		
		if (!selected.IsMoving()
				&& selected.IsMouseOver(Cam.GetPos()) && Cursor.isButtonJustPressed(Cursor.LEFT))
		{
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
			UpdateButtons(Cam.GetPos());
		
		if (moveactive)
			UpdateMove(Cam.GetPos());
	}
	
	private void UpdateButtons(Vector2 Campos)
	{
		// DO NOT UPDATE THE MENU IF NO SQUAD IS SELECTED
		if (selected == null)
			return;
		
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
			moveselect.SetPos((int)selected.GetBoundingBox().x, (int)selected.GetBoundingBox().width);
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
		if (selected == null)
			return;
		
		moveselect.Update(Campos);

		// set the target pos on left release, or cancel on right click
		if (Cursor.isButtonJustPressed(Cursor.LEFT)) {
			selected.SetTargetX(moveselect.GetTargetX());
			moveactive = false;
		} else if (Cursor.isButtonJustPressed(Cursor.RIGHT))
			moveactive = false;
	}
	
	private boolean HighlightSquad(Squad S, Camera Cam)
	{
		if (selected != S)
			return false;
		
		if (menuactive || moveactive)
			return true;
		
		if (selected.IsMouseOver(Cam.GetPos()) && !selected.IsMoving())
			return true;
		
		return false;
	}
	
	public void DrawBase(SpriteBatch Batch, Camera Cam)
	{
		base.Draw(Batch, Cam);
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam)
	{
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			Squad c = s.next();
			c.Draw(Batch, Cam, HighlightSquad(c, Cam));
		}
		
		if (menuactive && selected != null) {
			Rectangle bbox = selected.GetBoundingBox();
			menu.SetPos( (int)(bbox.x + bbox.width/2), (int)(bbox.y + bbox.height*1.5f), Cam.GetPos());
			menu.Draw(Batch, Cam);
		}
		
		if (moveactive && selected != null) {
			moveselect.Draw(Batch, Cam);
		}
	}
}
