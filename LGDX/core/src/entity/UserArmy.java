package entity;

import java.util.Iterator;

import terrain.Terrain;
import ui.ButtonOptions;
import ui.PointSelect;
import ui.UnitDeployer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Cursor;
import com.mygdx.game.MilitaryBase;

public class UserArmy extends Army
{
	private boolean menuactive;
	private boolean menurelease;
	private ButtonOptions menu;
	
	private int prevdeployi;
	private PointSelect moveselect;
	private boolean moveactive;
	
	private Squad selected; // the currently selected squad, or null
	
	public UserArmy(MilitaryBase Base, Terrain Ter)
	{
		base = Base;
		UnitDeployer.SetPos(base.GetPos());
		ter = Ter;
		
		menuactive = false;
		menurelease = false;
		
		menu = new ButtonOptions(0, 0, 4);
		menu.SetGlyph(0, ButtonOptions.ATTACK);
		menu.SetGlyph(1, ButtonOptions.MOVE);
		menu.SetGlyph(2, ButtonOptions.UPGRADE);
		menu.SetGlyph(3, ButtonOptions.STOP);
		
		moveselect = new PointSelect(Ter);
		moveactive = false;
		prevdeployi = -1;
		selected = null;
	}
	
	public void Update(Camera Cam)
	{
		super.Update(Cam);
		
		SetSelectedSquad(Cam.GetPos());
		UpdateMenu(Cam);
		UpdateDeployer(Cam);
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
	
	private void UpdateDeployer(Camera Cam)
	{
		int selected = UnitDeployer.GetSelected(Cam);
		
		if (selected >= 0 && selected < UnitDeployer.UNITCOUNT &&
			Cursor.isButtonJustReleased(Cursor.LEFT))
		{
			switch (selected) 
			{
			case UnitDeployer.GUNMAN:
				SpawnGunmen(5, Cam, 80);
				break;
			
			case UnitDeployer.SPECOPS:
				SpawnSpecops(5, Cam, 80);
				break;
				
			case UnitDeployer.STEALTHOPS:
				SpawnStealth(5, Cam, 80);
				break;
				
			default:
				break;
			}
		}
	}
	
	private void DrawDeployer(SpriteBatch Batch, Camera Cam)
	{
		Rectangle r0 = new Rectangle(base.GetPos().x+76, base.GetPos().y, 110, 79);
		Rectangle r1 = new Rectangle(base.GetPos().x+192, base.GetPos().y, 110, 79);
		Rectangle r2 = new Rectangle(base.GetPos().x+306, base.GetPos().y, 110, 79);
		
		int i = -1;
		if (Cursor.IsMouseOver(r0, Cam.GetPos()))
			i = 0;
		else if (Cursor.IsMouseOver(r1, Cam.GetPos()))
			i = 1;
		else if (Cursor.IsMouseOver(r2, Cam.GetPos()))
			i = 2;
		
		if (i != prevdeployi)
			UnitDeployer.ResetClock();
		prevdeployi = i;
			
		if (UnitDeployer.Contains(i))
			UnitDeployer.Draw(Batch, Cam, i);
		
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
	
	public void Draw(SpriteBatch Batch, Camera Cam)
	{
		DrawDeployer(Batch, Cam);
		
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
