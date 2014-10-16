package entity;

import java.util.Iterator;

import terrain.Terrain;
import ui.ButtonOptions;
import ui.PointSelect;
import ui.UnitDeployer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Cursor;
import com.mygdx.game.MilitaryBase;

public class UserArmy extends Army
{
	private SelectionStack optionstack;
	private int prevstacksize;
	
	private boolean menuactive;
	private boolean menurelease;
	private ButtonOptions menu;
	
	private int prevdeployi;
	private PointSelect moveselect;
	private boolean moveactive;
	
	private Squad selected; // the currently selected squad, or null
	
	public UserArmy(MilitaryBase Base, Terrain Ter, Camera Cam)
	{
		base = Base;
		UnitDeployer.SetPos(base.GetPos());
		SetDeployBBox(Cam);
		
		ter = Ter;
		
		optionstack = new SelectionStack();
		prevstacksize = 0;
		
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
	
	private void SetDeployBBox(Camera Cam)
	{
		Rectangle r0 = new Rectangle(base.GetPos().x+76, base.GetPos().y, 110, 79);
		Rectangle r1 = new Rectangle(base.GetPos().x+192, base.GetPos().y, 110, 79);
		Rectangle r2 = new Rectangle(base.GetPos().x+306, base.GetPos().y, 110, 79);
		
		UnitDeployer.SetBBox(r0, 0);
		UnitDeployer.SetBBox(r1, 1);
		UnitDeployer.SetBBox(r2, 2);
	}
	
	public void Update(Camera Cam)
	{
		super.Update(Cam);
		
		BuildOptionStack(Cam);
		SetSelected();
		UpdateDeployer(Cam);
		
		UpdateMenu(Cam);
	}
	
	private void BuildOptionStack(Camera Cam)
	{
		// do not process new information while a menu is open
		if (IsMenuOpen()) {
			optionstack.Reset();
			return;
		}
		
		// proces input
		ProcStackChange();
		int stacksize = CalcOptionStackSize(Cam);

		/*
		 *  no change occured leave the method and do not recalc the option stack
		 */
		if (stacksize == prevstacksize) {
			prevstacksize = stacksize;
			return;
		}

		// a change occured
		prevstacksize = stacksize;
		
		// reset the optionstack and recalculate its contents
		optionstack.Reset();
		
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			Squad c = s.next();
			
			if (c.IsMouseOver(Cam.GetPos()))
				optionstack.AddSquadOver(c);
		}
		
		
		int selected = UnitDeployer.GetSelected(Cam);
		if (UnitDeployer.Contains(selected))
			optionstack.AddBarracksOver();
	}
	
	private void ProcStackChange()
	{
		// cycle though current option stack
		if (Gdx.input.isKeyJustPressed(Keys.TAB))
			optionstack.IncSelection();
		
		if (Cursor.getScrollDirection() > 0)
			optionstack.DecSelection();
		else if (Cursor.getScrollDirection() < 0)
			optionstack.IncSelection();
	}
	
	private int CalcOptionStackSize(Camera Cam)
	{
		int size = 0;
		
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			Squad c = s.next();
			
			// size increases for each unit over
			if (c.IsMouseOver(Cam.GetPos()))
				size++;
		}
		
		// size increased if a the mouse is over a deployer
		int selected = UnitDeployer.GetSelected(Cam);
		if (UnitDeployer.Contains(selected))
			size++;
		
		// return the anticipated size of the optionstack
		return size;
	}
	
	private void SetSelected()
	{
		// do not select a squad while a menu is open
		if (IsMenuOpen())
			return;
		
		if (optionstack.IsOverSquad()) {
			selected = optionstack.GetSquadOver();
		} else {
			selected = null;
		}
	}
	
	private void UpdateDeployer(Camera Cam)
	{
		// do not update the deployer while a menu is open
		if (IsMenuOpen() || !optionstack.IsOverAdd())
			return;
		
		int selected = UnitDeployer.GetSelected(Cam);
		
		if (UnitDeployer.Contains(selected) &&
			Cursor.isButtonJustReleased(Cursor.LEFT))
		{
			switch (selected) 
			{
			case UnitDeployer.GUNMAN:
				SpawnGunmen(5, Cam, 80);
				optionstack.Reset();
				break;
			
			case UnitDeployer.SPECOPS:
				SpawnSpecops(5, Cam, 80);
				optionstack.Reset();
				break;
				
			case UnitDeployer.STEALTHOPS:
				SpawnStealth(5, Cam, 80);
				optionstack.Reset();
				break;
				
			default:
				break;
			}
		}
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
	
	private boolean IsMenuOpen()
	{
		return (menuactive || moveactive);
	}
	
	private void DrawDeployer(SpriteBatch Batch, Camera Cam)
	{
		// do not draw the deployer when a menu is open
		if (IsMenuOpen() || !optionstack.IsOverAdd())
			return;
		
		int i = UnitDeployer.GetSelected(Cam);
		
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
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			Squad c = s.next();
			
			boolean highlight = HighlightSquad(c, Cam);
			c.Draw(Batch, Cam, highlight);
		}
		
		DrawDeployer(Batch, Cam);
		
		// do not check for procmouseevent status, as the menus have priority
		if (menuactive && selected != null) {
			Rectangle bbox = selected.GetBoundingBox();
			menu.SetPos( (int)(bbox.x + bbox.width/2), (int)(bbox.y - 48), Cam.GetPos());
			menu.Draw(Batch, Cam);
		}
		
		if (moveactive && selected != null) {
			moveselect.Draw(Batch, Cam);
		}
	}
}
