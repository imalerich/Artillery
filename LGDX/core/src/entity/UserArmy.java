package entity;

import java.util.Iterator;
import java.util.Vector;

import network.NetworkManager;
import network.Response;
import physics.CombatResolver;
import physics.GameWorld;
import terrain.Terrain;
import ui.ButtonOptions;
import ui.MenuBar;
import ui.PointSelect;
import ui.PowerButtons;
import ui.Profile;
import ui.UnitDeployer;
import arsenal.Armament;

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
	private SelectionStack targetstack;
	private int prevOptionStackSize;
	private int prevTargetStackSize;
	
	private boolean menuactive;
	private boolean menurelease;
	private ButtonOptions menu;
	private ButtonOptions offensemenu;
	
	private int prevdeployi;
	private PointSelect moveselect;
	private PowerButtons powerselect;
	private boolean moveactive;
	private boolean profileactive;
	private boolean targetenemies;
	private boolean targetpoint;
	private boolean targetpower;
	
	private Squad selected; // the currently selected squad, or null
	
	public UserArmy(GameWorld World, MilitaryBase Base, Terrain Ter, NetworkManager Network, int Connection)
	{
		world = World;
		ter = Ter;
		base = Base;
		network = Network;
		squads = new Vector<Squad>();
		SetConnection(Connection);
		
		UnitDeployer.SetPos(base.GetPos());
		SetDeployBBox();
		
		optionstack = new SelectionStack();
		targetstack = new SelectionStack();
		prevOptionStackSize = 0;
		prevTargetStackSize = 0;
		
		menuactive = false;
		menurelease = false;
		
		menu = new ButtonOptions(0, 0, 3);
		menu.SetGlyph(0, ButtonOptions.MOVE);
		menu.SetGlyph(1, ButtonOptions.UPGRADE);
		menu.SetGlyph(2, ButtonOptions.STOP);
		
		offensemenu = new ButtonOptions(0, 0, 2);
		offensemenu.SetGlyph(0, ButtonOptions.ATTACK);
		offensemenu.SetGlyph(1, ButtonOptions.STOP);
		
		moveselect = new PointSelect(Ter);
		powerselect = new PowerButtons();
		moveactive = false;
		profileactive = false;
		squadspawned = false;
		targetenemies = false;
		targetpoint = false;
		targetpower = false;
		
		prevdeployi = -1;
		selected = null;
		
		stagecompleted = new boolean[GameWorld.STAGECOUNT];
		for (int i=0; i<GameWorld.STAGECOUNT; i++) {
			stagecompleted[i] = false;
		}
	}

	@Override
	public void CatchMessage(Response r) 
	{
		//
	}
	
	private void SetDeployBBox()
	{
		Rectangle r0 = new Rectangle(base.GetPos().x+76, base.GetPos().y, 110, 79);
		Rectangle r1 = new Rectangle(base.GetPos().x+192, base.GetPos().y, 110, 79);
		Rectangle r2 = new Rectangle(base.GetPos().x+306, base.GetPos().y, 110, 79);
		
		UnitDeployer.SetBBox(r0, 0);
		UnitDeployer.SetBBox(r1, 1);
		UnitDeployer.SetBBox(r2, 2);
	}
	
	@Override
	public boolean IsStageCompleted(int Stage)
	{
		Response r = new Response();
				
		switch (Stage) 
		{
		case GameWorld.MOVESELECT:
			// do not leave the stage while a menu is open
			if (IsMenuOpen())
				return false;
			
			// check the end turn conditions
			if (Gdx.input.isKeyJustPressed(Keys.ENTER))
				stagecompleted[Stage] = true;
			else if (MenuBar.IsEndTurn())
				stagecompleted[Stage] = true;
			
			if (stagecompleted[Stage]) {
				r.source = GetConnection();
				r.request = "MOVESELECT";
				r.b0 = true;
				
				network.GetClient().sendTCP(r);
			}
			
			return stagecompleted[Stage];
		
		case GameWorld.MOVEUPDATE:
			Iterator<Squad> s = squads.iterator();
			while (s.hasNext()) {
				if (s.next().IsMoving())
					return false;
			}
			
			r.source = GetConnection();
			r.request = "MOVEUPDATE";
			r.b0 = true;

			network.GetClient().sendTCP(r);
				
			return true;
			
		case GameWorld.ATTACKSELECT:
			// do not leave the stage while a menu is open
			if (IsMenuOpen())
				return false;
			
			// check the end turn conditions
			if (Gdx.input.isKeyJustPressed(Keys.ENTER))
				stagecompleted[Stage] = true;
			else if (MenuBar.IsEndTurn())
				stagecompleted[Stage] = true;
			
			if (stagecompleted[Stage]) {
				r.source = GetConnection();
				r.request = "ATTACKSELECT";
				r.b0 = true;
				
				network.GetClient().sendTCP(r);
			}
			
			return stagecompleted[Stage];
			
		case GameWorld.ATTACKUPDATE:
			return true;
			
		default:
			return true;
		}
	}
	
	@Override
	public void AddCombatData(CombatResolver Resolver)
	{
		// uses the power modifier for projectiles
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			Squad squad = s.next();
			
			// add each squad and its target to the combat resolver
			if (squad.GetTargetSquad() != null && 
					squad.GetArmament().GetType() == Armament.UNITTARGET) {
				Resolver.AddConflict(squad, squad.GetTargetSquad());
			} else if (squad.IsFiring() && squad.GetArmament().GetType() == Armament.POINTTARGET) {
				Resolver.AddProjectile(squad, powerselect.GetPower()/PowerButtons.MAXPOWER,
						squad.GetArmament().GetStrength());
			}
		}
	}
	
	@Override
	public boolean UpdateTargetOptions(int Size)
	{
		if (Size != prevTargetStackSize) {
			prevTargetStackSize = Size;
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void UpdateThreads() 
	{
		//
	}
	
	@Override
	public SelectionStack GetTargetOptions()
	{
		return targetstack;
	}
	
	@Override
	public void SetTargetSquad(Squad Target)
	{
		if (selected != null && targetenemies) {
			selected.SetTargetSquad(Target);
		}
	}
	
	@Override
	public boolean IsTargeting()
	{
		return targetenemies;
	}
	
	@Override
	public void InitStage(int NewStage)
	{
		super.InitStage(NewStage);
		squadspawned = false;
		
		if (NewStage == GameWorld.ATTACKSELECT) {
			targetstack.Reset();
			
			Iterator<Squad> s = squads.iterator();
			while (s.hasNext()) {
				Squad squad = s.next();
				squad.SetTargetSquad(null);
				squad.SetFiring(false);
			}
		}
		
		// set the new stage as not completed
		stagecompleted[NewStage] = false;
	}
	
	@Override
	public void UpdateMove(Camera Cam)
	{
		super.UpdateMove(Cam);
	}
	
	@Override
	public void UpdateMoveSelect(Camera Cam)
	{
		// do not update while waiting for others to complete
		if (stagecompleted[GameWorld.MOVESELECT])
			return;
		
		BuildOptionStack(Cam, true);
		SetSelected();
		UpdateDeployer(Cam);
		
		UpdateMenu(Cam);
	}
	
	@Override
	public void UpdateAttackSelect(Camera Cam)
	{
		// do not update while waiting for others to complete
		if (stagecompleted[GameWorld.ATTACKSELECT])
			return;
		
		BuildOptionStack(Cam, false);
		SetSelected();
		
		UpdateOffenseMenu(Cam);
	}
	
	private void BuildOptionStack(Camera Cam, boolean IncludeDeployer)
	{
		// do not process new information while a menu is open
		if (IsMenuOpen()) {
			optionstack.Reset();
			return;
		}
		
		// process input
		ProcStackChange();
		int stacksize = CalcOptionStackSize(Cam);

		/*
		 *  no change occurred leave the method and do not recalculate the option stack
		 */
		if (stacksize == prevOptionStackSize) {
			return;
		}

		// a change occurred
		prevOptionStackSize = stacksize;
		
		// reset the option stack and recalculate its contents
		optionstack.Reset();
		GetMouseOver(optionstack, Cam);
		
		if (!IncludeDeployer)
			return;
		
		int selected = UnitDeployer.GetSelected(Cam);
		if (UnitDeployer.Contains(selected))
			optionstack.AddBarracksOver();
	}
	
	private void ProcStackChange()
	{
		// remove all invalid references from the target stack
		if (selected != null && targetstack.GetSize() != 0) {
			Iterator<SelectionElement> i = targetstack.GetIterator();
			while (i.hasNext()) {
				if (!selected.IsIntersectingView(i.next().ref.GetBBox())) {
					i.remove();
				}
			}
		}
		
		// cycle though current option stack
		if (Gdx.input.isKeyJustPressed(Keys.TAB)) {
			optionstack.IncSelection();
			targetstack.IncSelection();
		}
		
		if (Cursor.getScrollDirection() > 0) {
			optionstack.DecSelection();
			targetstack.DecSelection();
		} else if (Cursor.getScrollDirection() < 0) {
			optionstack.IncSelection();
			targetstack.IncSelection();
		}
	}
	
	private int CalcOptionStackSize(Camera Cam)
	{
		int size = 0;
		size += GetMouseOverCount(Cam);
		
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
		if (IsMenuOpen() || !optionstack.IsOverAdd() || squadspawned)
			return;
		
		int selected = UnitDeployer.GetSelected(Cam);
		
		if (UnitDeployer.Contains(selected) &&
			Cursor.isButtonJustReleased(Cursor.LEFT))
		{
			int id = SpawnUnit(selected);
			squadspawned = true;
			
			// tell all clients of the newly spawned squad
			Response r = new Response();
			r.source = GetConnection();
			r.request = "SQUADSPAWNED";
			r.i0 = selected;
			r.i1 = id;
			
			network.GetClient().sendTCP(r);
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
			} else if (!IsMenuOpen()) {
				menurelease = false;
				menuactive = true;
			}
		}
		
		if (menuactive) 
			UpdateButtons(Cam.GetPos());
		
		if (moveactive)
			UpdateMove(Cam.GetPos());
		
		if (profileactive)
			UpdateProfile();
	}
	
	private void UpdateOffenseMenu(Camera Cam)
	{
		if (selected == null)
			return;
		
		if (!selected.IsMoving()
				&& selected.IsMouseOver(Cam.GetPos()) && Cursor.isButtonJustPressed(Cursor.LEFT))
		{
			if (menurelease) {
				menuactive = false;
				menurelease = false;
				offensemenu.ResetClock();
			} else if (!IsMenuOpen()) {
				menurelease = false;
				menuactive = true;
			}
		}
		
		if (menuactive)
			UpdateOffenseButtons(Cam.GetPos());
		
		else if (targetenemies)
			UpdateTargetSquads();
		
		else if (targetpoint)
			UpdateTargetPoint(Cam);
		
		else if (targetpower)
			UpdateTargetPower(Cam);
	}
	
	private void UpdateButtons(Vector2 Campos)
	{
		// DO NOT UPDATE THE MENU IF NO SQUAD IS SELECTED
		if (selected == null) {
			menuactive = false;
			return;
		}
		
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
			selected.SetTargetX(-1);
			
			// tell all clients which squad is moving
			Response r = new Response();
			r.request = "SQUADMOVE";
			r.i0 = selected.GetID();
			r.i1 = selected.GetTargetX();
			r.source = GetConnection();
			
			network.GetClient().sendTCP(r);
			
			menuactive = false;
			menurelease = false;
			menu.ResetClock();
			break;

		case ButtonOptions.MOVE:
			// set the movement
			moveactive = true;
			moveselect.SetPos((int)selected.GetBoundingBox().x, (int)selected.GetBoundingBox().width);
			moveselect.SetMaxDist(selected.GetMoveDist());

			// leave the menu
			menuactive = false;
			menurelease = false;
			menu.ResetClock();
			break;
			
		case ButtonOptions.UPGRADE:
			// activate the profile
			profileactive = true;
			
			// leave the menu
			menuactive = false;
			menurelease = false;
			menu.ResetClock();
			Profile.ResetPos();
			break;

		default:
			break;
		}
	}
	
	private void UpdateOffenseButtons(Vector2 Campos)
	{
		// DO NOT UPDATE THE MENU IF NO SQUAD IS SELECTED
		if (selected == null) {
			menuactive = false;
			return;
		}
		
		if (Cursor.isButtonJustPressed(Cursor.RIGHT)) {
			menuactive = false;
			menurelease  = false;
			offensemenu.ResetClock();
		}
		
		if (!Cursor.isButtonPressed(Cursor.LEFT))
			menurelease = true;
		
		int event = -1;
		if (Cursor.isButtonJustReleased(Cursor.LEFT))
			event = offensemenu.GetAction( offensemenu.GetButtonDown(Campos) );
		
		switch (event)
		{
		case ButtonOptions.STOP:
			// leave the menu and do not select a squad
			selected.SetTargetSquad(null);
			selected.SetFiring(false);
			
			menuactive = false;
			menurelease = false;
			offensemenu.ResetClock();
			break;
			
		case ButtonOptions.ATTACK:
			// leave the menu
			menuactive = false;
			menurelease = false;
			offensemenu.ResetClock();
			
			if (selected.GetArmament().GetType() == Armament.UNITTARGET) {
				targetenemies = true;
			} else if (selected.GetArmament().GetType() == Armament.POINTTARGET) {
				targetpoint = true;
			}
			
			break;
			
		default:
			break;
		}
	}
	
	private void UpdateMove(Vector2 Campos)
	{
		if (selected == null) {
			moveactive = false;
			return;
		}
		
		moveselect.Update(Campos);

		// set the target position on left release, or cancel on right click
		if (Cursor.isButtonJustPressed(Cursor.LEFT)) {
			selected.SetTargetX(moveselect.GetTargetX());
			moveactive = false;
			
			// tell all clients which squad is moving
			Response r = new Response();
			r.request = "SQUADMOVE";
			r.i0 = selected.GetID();
			r.i1 = moveselect.GetTargetX();
			r.source = GetConnection();
			
			network.GetClient().sendTCP(r);
			
		} else if (Cursor.isButtonJustPressed(Cursor.RIGHT))
			moveactive = false;
	}
	
	private void UpdateTargetSquads()
	{
		// INVALID - do not select a unit
		if (selected == null) {
			targetenemies = false;
			return;
		}
		
		// leave targeting, do not select a unit
		if (Cursor.isButtonJustPressed(Cursor.RIGHT)) {
			selected.SetTargetSquad(null);
			targetenemies = false;
			return;
		}
		
		// leave the current unit selected and leave targeting for this squad
		if (Cursor.isButtonJustPressed(Cursor.LEFT)) {
			targetenemies = false;
			return;
		}
		
		// check stack changes
		Squad prev = selected.GetTargetSquad();
		ProcStackChange();
		Squad t = targetstack.GetSquadOver();
		selected.SetTargetSquad(t);
		if (t != null) {
			t.SetAsTarget();
		}
	
		// make sure a change was made to the selected target
		if (prev != selected.GetTargetSquad() && t != null) {
			
			// send the information to all clients
			Response r = new Response();
			r.request = "UNITTARGET";
			r.source = GetConnection();
			r.i0 = selected.GetID(); // unit that will be shooting
			r.i1 = t.GetArmy().GetConnection(); // army that is being shot at
			r.i2 = t.GetID(); // squad that is being shot at
			
			network.GetClient().sendTCP(r);
		}
	}
	
	private void UpdateTargetPoint(Camera Cam)
	{
		// INVALID - do not select a point
		if (selected == null) {
			targetpoint = false;
			return;
		}
		
		// leave targeting, do not select a point
		if (Cursor.isButtonJustPressed(Cursor.RIGHT)) {
			targetpoint = false;
			return;
		}
		
		// leave the current unit selected and leave targeting for this squad
		if (Cursor.isButtonJustReleased(Cursor.LEFT)) {
			selected.SetFiring(true);
			targetpoint = false;
			targetpower = true;
			powerselect.SetPos(Cursor.GetMouseX(Cam.GetPos())+Cam.GetPos().x, Cursor.GetMouseY()+Cam.GetPos().y);
			
			return;
		}
		
		// set the angle for the barrel
		float xpos = selected.GetBBox().x + selected.GetBBox().width/2f;
		float ypos = selected.GetBBox().y + selected.GetBBox().height/2f;
		Vector2 sourcepos = new Vector2(xpos, ypos);
		
		xpos = Cursor.GetMouseX(Cam.GetPos()) + Cam.GetPos().x;
		ypos = Cursor.GetMouseY() + Cam.GetPos().y;
		Vector2 destpos = new Vector2(xpos, ypos);
		
		// do not set the angle when the mouse is on the incorrect side of the selected unit
		float startx = selected.GetBBox().x + selected.GetBBox().width/2f;
		int direction = GameWorld.GetDirection(startx, 0f, 
				xpos, 0f);
		if (direction != 1 && selected.IsForward()) {
			selected.SetForward(false);
		} else if (direction != -1 && !selected.IsForward()) {
			selected.SetForward(true);
		}
		
		float xdist = GameWorld.GetDistance(startx, 0f, xpos, 0f);
		float ydist = destpos.y - sourcepos.y;
		float theta = (float)( Math.toDegrees(Math.atan(ydist/xdist)) );
		
		selected.SetFiring(false);
		selected.SetBarrelAngle(theta);
	}
	
	private void UpdateTargetPower(Camera Cam)
	{
		// INVALID - do not select a point
		if (selected == null) {
			targetpower = false;
			return;
		}
		
		// leave targeting, do not select a point
		if (Cursor.isButtonJustPressed(Cursor.RIGHT)) {
			selected.SetFiring(false);
			targetpower = false;
			
			return;
		}
		
		// leave the current unit selected and leave targeting for this squad
		if (powerselect.DoFire(Cam)) {
			selected.SetFiring(true);
			targetpower = false;
			
			// send a message to all clients informing them who is firing and where
			Response r = new Response();
			r.source = GetConnection();
			r.request = "TANKFIRING";
			r.i0 = selected.GetID();
			r.b0 = selected.IsFiring();
			r.b1 = selected.IsForward();
			r.f0 = selected.GetBarrelAngle();
			r.f1 = powerselect.GetPower()/PowerButtons.MAXPOWER;
			
			network.GetClient().sendTCP(r);
			
			return;
		}
		
		powerselect.Update(Cam);
		MenuBar.SetPowerLevel(powerselect.GetPower(), PowerButtons.MAXPOWER);
	}
	
	private void UpdateProfile()
	{
		if (selected == null) {
			profileactive = false;
			return;
		}
			
		if (Profile.IsMouseOverClose() && Cursor.isButtonJustReleased(Cursor.LEFT))
			profileactive = false;
	}
	
	@Override
	public boolean IsMenuOpen()
	{
		return (menuactive || moveactive || profileactive || targetenemies || targetpoint || targetpower);
	}
	
	private void DrawDeployer(SpriteBatch Batch, Camera Cam)
	{
		// do not draw the deployer when a menu is open
		if (IsMenuOpen() || !optionstack.IsOverAdd() || squadspawned)
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
		
		if (IsMenuOpen())
			return true;
		
		if (selected.IsMouseOver(Cam.GetPos()) && !selected.IsMoving())
			return true;
		
		return false;
	}
	
	@Override
	public void DrawTargetPos(SpriteBatch Batch, Camera Cam)
	{
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			s.next().DrawTargetPos(Batch, Cam);
		}
	}
	
	@Override
	public void DrawTargetSquad(SpriteBatch Batch, Camera Cam)
	{
		// do not draw while in the power selection menu
		if (targetpower)
			return;
		
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			s.next().DrawTargetSquad(Batch, Cam);
		}
	}
	
	@Override
	public void Draw(SpriteBatch Batch, Camera Cam, boolean CheckTargets, int CurrentStage)
	{
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			Squad c = s.next();
			
			boolean highlight = HighlightSquad(c, Cam);
			c.Draw(Batch, Cam, highlight);
		}
		
		DrawDeployer(Batch, Cam);
		
		if (menuactive && selected != null) {
			Rectangle bbox = selected.GetBoundingBox();
			
			if (CurrentStage == GameWorld.MOVESELECT) {
				menu.SetPos( (int)(bbox.x + bbox.width/2), (int)(bbox.y - 48), Cam.GetPos());
				menu.Draw(Batch, Cam);
			} else if (CurrentStage == GameWorld.ATTACKSELECT) {
				offensemenu.SetPos( (int)(bbox.x + bbox.width/2), (int)(bbox.y - 48), Cam.GetPos());
				offensemenu.Draw(Batch, Cam);
			}
		}
		
		if (moveactive && selected != null) {
			moveselect.Draw(Batch, Cam);
		}
		
		if (targetpower && selected != null) {
			powerselect.Draw(Batch, Cam);
		}
		
		if (profileactive && selected != null) {
			Profile.Draw(Batch, selected, base.GetLogo());
		}
	}
}
