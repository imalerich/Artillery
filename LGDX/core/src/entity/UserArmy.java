package entity;

import java.util.Iterator;
import java.util.Vector;

import objects.FoxHole;
import network.NetworkManager;
import network.Response;
import physics.CombatResolver;
import physics.GameWorld;
import terrain.Terrain;
import ui.ButtonOptions;
import ui.FoxHoleMenu;
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

import config.SquadConfigurations;

public class UserArmy extends Army
{
	public static final int REQBONUS = 100;
	
	private SelectionStack optionstack;
	private SelectionStack targetstack;
	private int prevOptionStackSize;
	private int prevTargetStackSize;
	
	private boolean menuactive;
	private boolean menurelease;
	private ButtonOptions menu;
	private ButtonOptions offensemenu;
	private FoxHoleMenu foxselect;
	
	private int prevdeployi;
	private PointSelect moveselect;
	private PowerButtons powerselect;
	private boolean moveactive;
	private boolean foxactive;
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
		setConnection(Connection);
		
		UnitDeployer.setPos(base.getPos());
		setDeployBBox();
		
		optionstack = new SelectionStack();
		targetstack = new SelectionStack();
		prevOptionStackSize = 0;
		prevTargetStackSize = 0;
		
		menuactive = false;
		menurelease = false;
		foxactive = false;
		
		menu = new ButtonOptions(0, 0, 4);
		menu.setGlyph(0, ButtonOptions.MOVE);
		menu.setGlyph(1, ButtonOptions.MOVEFOXHOLE);
		menu.setGlyph(2, ButtonOptions.UPGRADE);
		menu.setGlyph(3, ButtonOptions.STOP);
		
		offensemenu = new ButtonOptions(0, 0, 2);
		offensemenu.setGlyph(0, ButtonOptions.ATTACK);
		offensemenu.setGlyph(1, ButtonOptions.STOP);
		
		foxselect = new FoxHoleMenu(Ter);
		
		moveselect = new PointSelect(Ter);
		powerselect = new PowerButtons();
		moveactive = false;
		profileactive = false;
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
	public void catchMessage(Response r) 
	{
		//
	}
	
	private void setDeployBBox()
	{
		Rectangle r0 = new Rectangle(base.getPos().x+76, base.getPos().y, 110, 79);
		Rectangle r1 = new Rectangle(base.getPos().x+192, base.getPos().y, 110, 79);
		Rectangle r2 = new Rectangle(base.getPos().x+306, base.getPos().y, 110, 79);
		
		UnitDeployer.setBBox(r0, 0);
		UnitDeployer.setBBox(r1, 1);
		UnitDeployer.setBBox(r2, 2);
	}
	
	@Override
	public boolean isStageCompleted(int Stage)
	{
		Response r = new Response();
				
		switch (Stage) 
		{
		case GameWorld.MOVESELECT:
			// do not leave the stage while a menu is open
			if (isMenuOpen())
				return false;
			
			// check the end turn conditions
			if (Gdx.input.isKeyJustPressed(Keys.ENTER))
				stagecompleted[Stage] = true;
			else if (MenuBar.isEndTurn())
				stagecompleted[Stage] = true;
			
			if (stagecompleted[Stage]) {
				r.source = getConnection();
				r.request = "MOVESELECT";
				r.b0 = true;
				
				network.getClient().sendTCP(r);
			}
			
			return stagecompleted[Stage];
		
		case GameWorld.MOVEUPDATE:
			Iterator<Squad> s = squads.iterator();
			while (s.hasNext()) {
				if (s.next().isMoving())
					return false;
			}
			
			r.source = getConnection();
			r.request = "MOVEUPDATE";
			r.b0 = true;

			network.getClient().sendTCP(r);
				
			return true;
			
		case GameWorld.ATTACKSELECT:
			// do not leave the stage while a menu is open
			if (isMenuOpen())
				return false;
			
			// check the end turn conditions
			if (Gdx.input.isKeyJustPressed(Keys.ENTER))
				stagecompleted[Stage] = true;
			else if (MenuBar.isEndTurn())
				stagecompleted[Stage] = true;
			
			if (stagecompleted[Stage]) {
				r.source = getConnection();
				r.request = "ATTACKSELECT";
				r.b0 = true;
				
				network.getClient().sendTCP(r);
			}
			
			return stagecompleted[Stage];
			
		case GameWorld.ATTACKUPDATE:
			return true;
			
		default:
			return true;
		}
	}
	
	@Override
	public void addCombatData(CombatResolver Resolver)
	{
		// uses the power modifier for projectiles
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			Squad squad = s.next();
			
			// add each squad and its target to the combat resolver
			if (squad.getTargetSquad() != null && 
					squad.getArmament().getType() == Armament.UNITTARGET) {
				Resolver.addConflict(squad, squad.getTargetSquad());
			} else if (squad.isFiring() && squad.getArmament().getType() == Armament.POINTTARGET) {
				Resolver.addProjectile(squad, powerselect.getPower()/PowerButtons.MAXPOWER,
						squad.getArmament().getStrength());
			}
		}
	}
	
	@Override
	public boolean updateTargetOptions(int Size)
	{
		if (Size != prevTargetStackSize) {
			prevTargetStackSize = Size;
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void updateThreads(Camera Cam) 
	{
		//
	}
	
	@Override
	public SelectionStack getTargetOptions()
	{
		return targetstack;
	}
	
	@Override
	public void setTargetSquad(Squad Target)
	{
		if (selected != null && targetenemies) {
			selected.setTargetSquad(Target);
		}
	}
	
	@Override
	public boolean isTargeting()
	{
		return targetenemies;
	}
	
	@Override
	public void initStage(Camera Cam, int NewStage)
	{
		super.initStage(Cam, NewStage);
		
		if (NewStage == GameWorld.ATTACKSELECT) {
			targetstack.reset();
			
			Iterator<Squad> s = squads.iterator();
			while (s.hasNext()) {
				Squad squad = s.next();
				squad.setTargetSquad(null);
				squad.setFiring(false);
			}
			
			checkForFoxOccupancy(Cam.getPos());
		} else if (NewStage == GameWorld.MOVESELECT) {
			requisition += REQBONUS;
		}
		
		// set the new stage as not completed
		stagecompleted[NewStage] = false;
	}
	
	@Override
	public void updateMove(Camera Cam)
	{
		super.updateMove(Cam);
	}
	
	@Override
	public void updateMoveSelect(Camera Cam)
	{
		// do not update while waiting for others to complete
		if (stagecompleted[GameWorld.MOVESELECT])
			return;
		
		buildOptionStack(Cam, true);
		setSelected();
		updateDeployer(Cam);
		
		updateMenu(Cam);
	}
	
	@Override
	public void updateAttackSelect(Camera Cam)
	{
		// do not update while waiting for others to complete
		if (stagecompleted[GameWorld.ATTACKSELECT])
			return;
		
		buildOptionStack(Cam, false);
		setSelected();
		
		updateOffenseMenu(Cam);
	}
	
	private void buildOptionStack(Camera Cam, boolean IncludeDeployer)
	{
		// do not process new information while a menu is open
		if (isMenuOpen()) {
			optionstack.reset();
			return;
		}
		
		// process input
		procStackChange();
		int stacksize = calcOptionStackSize(Cam);

		/*
		 *  no change occurred leave the method and do not recalculate the option stack
		 */
		if (stacksize == prevOptionStackSize) {
			return;
		}

		// a change occurred
		prevOptionStackSize = stacksize;
		
		// reset the option stack and recalculate its contents
		optionstack.reset();
		getMouseOver(optionstack, Cam, false);
		
		if (!IncludeDeployer)
			return;
		
		int selected = UnitDeployer.getSelected(Cam);
		if (UnitDeployer.contains(selected))
			optionstack.addBarracksOver();
	}
	
	private void procStackChange()
	{
		// remove all invalid references from the target stack
		if (selected != null && targetstack.getSize() != 0) {
			Iterator<SelectionElement> i = targetstack.getIterator();
			while (i.hasNext()) {
				if (!selected.isIntersectingView(i.next().ref.getBBox())) {
					i.remove();
				}
			}
		}
		
		// cycle though current option stack
		if (Gdx.input.isKeyJustPressed(Keys.TAB)) {
			optionstack.incSelection();
			targetstack.incSelection();
		}
		
		if (Cursor.getScrollDirection() > 0) {
			optionstack.decSelection();
			targetstack.decSelection();
		} else if (Cursor.getScrollDirection() < 0) {
			optionstack.incSelection();
			targetstack.incSelection();
		}
	}
	
	private int calcOptionStackSize(Camera Cam)
	{
		int size = 0;
		size += getMouseOverCount(Cam);
		
		// size increased if a the mouse is over a deployer
		int selected = UnitDeployer.getSelected(Cam);
		if (UnitDeployer.contains(selected))
			size++;
		
		// return the anticipated size of the optionstack
		return size;
	}
	
	private void setSelected()
	{
		// do not select a squad while a menu is open
		if (isMenuOpen())
			return;
		
		if (optionstack.isOverSquad()) {
			selected = optionstack.getSquadOver();
		} else {
			selected = null;
		}
	}
	
	private void updateDeployer(Camera Cam)
	{
		// do not update the deployer while a menu is open
		if (isMenuOpen() || !optionstack.isOverAdd())
			return;
		
		int selected = UnitDeployer.getSelected(Cam);
		
		if (UnitDeployer.contains(selected) &&
			Cursor.isButtonJustReleased(Cursor.LEFT))
		{
			// get the requisition post spawning the unit
			int postcost = reqPostDeploy(selected);
			if (postcost < 0) {
				// user cannot afford to spawn the unit
				return;
			}
			
			// go ahead and spawn the unit
			requisition = postcost;
			int id = spawnUnit(selected);
			
			// tell all clients of the newly spawned squad
			Response r = new Response();
			r.source = getConnection();
			r.request = "SQUADSPAWNED";
			r.i0 = selected;
			r.i1 = id;
			
			network.getClient().sendTCP(r);
		}
	}
	
	private int reqPostDeploy(int Selected)
	{
		// get the cost of the selected unit
		switch (Selected) {
		case UnitDeployer.GUNMAN:
			int r = requisition - SquadConfigurations.getConfiguration(SquadConfigurations.GUNMAN).reqcost;
			return r;
			
		case UnitDeployer.SPECOPS:
			return requisition - SquadConfigurations.getConfiguration(SquadConfigurations.SPECOPS).reqcost;
			
		case UnitDeployer.STEALTHOPS:
			return requisition - SquadConfigurations.getConfiguration(SquadConfigurations.STEALTHOPS).reqcost;
			
		default:
			return requisition;
		}
	}
	
	private void updateMenu(Camera Cam)
	{
		if (selected == null)
			return;
		
		if (!selected.isMoving()
				&& selected.isMouseOver(Cam.getPos()) && Cursor.isButtonJustPressed(Cursor.LEFT))
		{
			if (menurelease) {
				menuactive = false;
				menurelease = false;
				menu.resetClock();
			} else if (!isMenuOpen()) {
				menurelease = false;
				menuactive = true;
			}
		}
		
		if (menuactive) 
			updateButtons(Cam.getPos());
		
		if (moveactive)
			updateMove(Cam.getPos());
		
		if (profileactive)
			updateProfile();
		
		if (foxactive)
			updateFox(Cam);
	}
	
	private void updateOffenseMenu(Camera Cam)
	{
		if (selected == null)
			return;
		
		if (!selected.isMoving()
				&& selected.isMouseOver(Cam.getPos()) && Cursor.isButtonJustPressed(Cursor.LEFT))
		{
			if (menurelease) {
				menuactive = false;
				menurelease = false;
				offensemenu.resetClock();
			} else if (!isMenuOpen()) {
				menurelease = false;
				menuactive = true;
			}
		}
		
		if (menuactive)
			updateOffenseButtons(Cam.getPos());
		
		else if (targetenemies)
			updateTargetSquads();
		
		else if (targetpoint)
			updateTargetPoint(Cam);
		
		else if (targetpower)
			updateTargetPower(Cam);
	}
	
	private void updateButtons(Vector2 Campos)
	{
		// DO NOT UPDATE THE MENU IF NO SQUAD IS SELECTED
		if (selected == null) {
			menuactive = false;
			return;
		}
		
		if (selected.getArmament().getType() == Armament.POINTTARGET) {
			menu.setSkip(1);
		} else {
			menu.noSkip();
		}
		
		if (Cursor.isButtonJustPressed(Cursor.RIGHT)) {
			menuactive = false;
			menurelease  = false;
			menu.resetClock();
		}
		
		if (!Cursor.isButtonPressed(Cursor.LEFT))
			menurelease = true;
		
		int event = -1;
		if (Cursor.isButtonJustReleased(Cursor.LEFT))
			event = menu.getAction( menu.getButtonDown(Campos) );
		
		switch (event)
		{
		case ButtonOptions.STOP:
			// leave the menu
			selected.setTargetX(-1);
			
			// tell all clients which squad is moving
			Response r = new Response();
			r.request = "SQUADMOVE";
			r.i0 = selected.getID();
			r.i1 = selected.getTargetX();
			r.source = getConnection();
			
			network.getClient().sendTCP(r);
			
			menuactive = false;
			menurelease = false;
			menu.resetClock();
			break;

		case ButtonOptions.MOVE:
			// set the movement
			moveactive = true;
			moveselect.setPos((int)selected.getBoundingBox().x, (int)selected.getBoundingBox().width);
			moveselect.setMaxDist(selected.getMoveDist());

			// leave the menu
			menuactive = false;
			menurelease = false;
			menu.resetClock();
			break;
			
		case ButtonOptions.MOVEFOXHOLE:
			if (requisition - 100 >= 0) {
				foxselect.setSelected(selected);
				foxactive = true;
			}
			
			// leave the menu
			menuactive = false;
			menurelease = false;
			menu.resetClock();
			break;
			
		case ButtonOptions.UPGRADE:
			// activate the profile
			profileactive = true;
			
			// leave the menu
			menuactive = false;
			menurelease = false;
			menu.resetClock();
			Profile.resetPos();
			break;

		default:
			break;
		}
	}
	
	private void updateFox(Camera Cam)
	{
		if (Cursor.isButtonPressed(Cursor.RIGHT) || selected == null) {
			foxactive = false;
			return;
		}
		
		if (Cursor.isButtonJustPressed(Cursor.LEFT)) {
			if (foxselect.isPosValid()) {
				foxselect.setSelectedTarget(selected);
			}
			
			foxactive = false;
			return;
		}
		
		foxselect.update(Cam);
	}
	
	public void addFox(Vector2 Pos)
	{
		FoxHoleMenu.cutRoom(ter, Pos);
		world.addFoxHole(Pos);
		System.out.println("Pos.y: " + Pos.y);
		
		// inform all clients of the added fox hole
		Response r = new Response();
		r.source = getConnection();
		r.request = "ADDFOX";
		r.f0 = Pos.x;
		r.f1 = Pos.y;
		System.out.println("r.f1: " + r.f1);

		network.getClient().sendTCP(r);
	}
	
	private void updateOffenseButtons(Vector2 Campos)
	{
		// DO NOT UPDATE THE MENU IF NO SQUAD IS SELECTED
		if (selected == null) {
			menuactive = false;
			return;
		}
		
		if (Cursor.isButtonJustPressed(Cursor.RIGHT)) {
			menuactive = false;
			menurelease  = false;
			offensemenu.resetClock();
		}
		
		if (!Cursor.isButtonPressed(Cursor.LEFT))
			menurelease = true;
		
		int event = -1;
		if (Cursor.isButtonJustReleased(Cursor.LEFT))
			event = offensemenu.getAction( offensemenu.getButtonDown(Campos) );
		
		switch (event)
		{
		case ButtonOptions.STOP:
			// leave the menu and do not select a squad
			selected.setTargetSquad(null);
			selected.setFiring(false);
			
			menuactive = false;
			menurelease = false;
			offensemenu.resetClock();
			break;
			
		case ButtonOptions.ATTACK:
			// leave the menu
			menuactive = false;
			menurelease = false;
			offensemenu.resetClock();
			
			if (selected.getArmament().getType() == Armament.UNITTARGET) {
				targetenemies = true;
			} else if (selected.getArmament().getType() == Armament.POINTTARGET) {
				targetpoint = true;
			}
			
			break;
			
		default:
			break;
		}
	}
	
	private void updateMove(Vector2 Campos)
	{
		if (selected == null) {
			moveactive = false;
			return;
		}
		
		moveselect.update(Campos);

		// set the target position on left release, or cancel on right click
		if (Cursor.isButtonJustPressed(Cursor.LEFT)) {
			selected.setTargetX(moveselect.getTargetX());
			moveactive = false;
			
			// tell all clients which squad is moving
			Response r = new Response();
			r.request = "SQUADMOVE";
			r.i0 = selected.getID();
			r.i1 = moveselect.getTargetX();
			r.source = getConnection();
			
			network.getClient().sendTCP(r);
			
		} else if (Cursor.isButtonJustPressed(Cursor.RIGHT))
			moveactive = false;
	}
	
	private void updateTargetSquads()
	{
		// INVALID - do not select a unit
		if (selected == null) {
			targetenemies = false;
			return;
		}
		
		// leave targeting, do not select a unit
		if (Cursor.isButtonJustPressed(Cursor.RIGHT)) {
			selected.setTargetSquad(null);
			targetenemies = false;
			return;
		}
		
		// leave the current unit selected and leave targeting for this squad
		if (Cursor.isButtonJustPressed(Cursor.LEFT)) {
			targetenemies = false;
			return;
		}
		
		// check stack changes
		Squad prev = selected.getTargetSquad();
		procStackChange();
		Squad t = targetstack.getSquadOver();
		selected.setTargetSquad(t);
		if (t != null) {
			t.setAsTarget();
		}
	
		// make sure a change was made to the selected target
		if (prev != selected.getTargetSquad() && t != null) {
			
			// send the information to all clients
			Response r = new Response();
			r.request = "UNITTARGET";
			r.source = getConnection();
			r.i0 = selected.getID(); // unit that will be shooting
			r.i1 = t.getArmy().getConnection(); // army that is being shot at
			r.i2 = t.getID(); // squad that is being shot at
			
			network.getClient().sendTCP(r);
		}
	}
	
	private void updateTargetPoint(Camera Cam)
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
			selected.setFiring(true);
			targetpoint = false;
			targetpower = true;
			powerselect.setPos(Cursor.getMouseX(Cam.getPos())+Cam.getPos().x, Cursor.getMouseY()+Cam.getPos().y);
			
			return;
		}
		
		// set the angle for the barrel
		float xpos = selected.getBBox().x + selected.getBBox().width/2f;
		float ypos = selected.getBBox().y + selected.getBBox().height/2f;
		Vector2 sourcepos = new Vector2(xpos, ypos);
		
		xpos = Cursor.getMouseX(Cam.getPos()) + Cam.getPos().x;
		ypos = Cursor.getMouseY() + Cam.getPos().y;
		Vector2 destpos = new Vector2(xpos, ypos);
		
		// do not set the angle when the mouse is on the incorrect side of the selected unit
		float startx = selected.getBBox().x + selected.getBBox().width/2f;
		int direction = GameWorld.getDirection(startx, 0f, 
				xpos, 0f);
		if (direction != 1 && selected.isForward()) {
			selected.setForward(false);
		} else if (direction != -1 && !selected.isForward()) {
			selected.setForward(true);
		}
		
		float xdist = GameWorld.getDistance(startx, 0f, xpos, 0f);
		float ydist = destpos.y - sourcepos.y;
		float theta = (float)( Math.toDegrees(Math.atan(ydist/xdist)) );
		
		selected.setFiring(false);
		selected.setBarrelAngle(theta);
	}
	
	private void updateTargetPower(Camera Cam)
	{
		// INVALID - do not select a point
		if (selected == null) {
			targetpower = false;
			return;
		}
		
		// leave targeting, do not select a point
		if (Cursor.isButtonJustPressed(Cursor.RIGHT)) {
			selected.setFiring(false);
			targetpower = false;
			
			return;
		}
		
		// leave the current unit selected and leave targeting for this squad
		if (powerselect.doFire(Cam)) {
			selected.setFiring(true);
			targetpower = false;
			
			// send a message to all clients informing them who is firing and where
			Response r = new Response();
			r.source = getConnection();
			r.request = "TANKFIRING";
			r.i0 = selected.getID();
			r.b0 = selected.isFiring();
			r.b1 = selected.isForward();
			r.f0 = selected.getBarrelAngle();
			r.f1 = powerselect.getPower()/PowerButtons.MAXPOWER;
			
			network.getClient().sendTCP(r);
			
			return;
		}
		
		powerselect.update(Cam);
		MenuBar.setPowerLevel(powerselect.getPower(), PowerButtons.MAXPOWER);
	}
	
	private void updateProfile()
	{
		if (selected == null) {
			profileactive = false;
			return;
		}
			
		if (Profile.isMouseOverClose() && Cursor.isButtonJustReleased(Cursor.LEFT))
			profileactive = false;
	}
	
	@Override
	public boolean isMenuOpen()
	{
		return (menuactive || moveactive || profileactive || targetenemies || targetpoint || targetpower || foxactive);
	}
	
	private void drawDeployer(SpriteBatch Batch, Camera Cam)
	{
		// do not draw the deployer when a menu is open
		if (isMenuOpen() || !optionstack.isOverAdd())
			return;
		
		int i = UnitDeployer.getSelected(Cam);
		
		if (i != prevdeployi)
			UnitDeployer.resetClock();
		prevdeployi = i;
			
		if (UnitDeployer.contains(i))
			UnitDeployer.draw(Batch, Cam, i);
		
		int postcost = reqPostDeploy(i);
		MenuBar.setTmpRequisition(postcost);
	}
	
	private boolean highlightSquad(Squad S, Camera Cam)
	{
		if (selected != S)
			return false;
		
		if (isMenuOpen())
			return true;
		
		if (selected.isMouseOver(Cam.getPos()) && !selected.isMoving())
			return true;
		
		return false;
	}
	
	@Override
	public void drawTargetPos(SpriteBatch Batch, Camera Cam)
	{
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			s.next().drawTargetPos(Batch, Cam);
		}
	}
	
	@Override
	public void drawTargetSquad(SpriteBatch Batch, Camera Cam)
	{
		// do not draw while in the power selection menu
		if (targetpower)
			return;
		
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			s.next().drawTargetSquad(Batch, Cam);
		}
	}
	
	private void checkReqCosts(Camera Cam)
	{
		//
		MenuBar.setRequisition(requisition);
		MenuBar.setTmpRequisition(requisition);
		
		// check for actions that cost requisition
		if (menuactive) {
			if (menu.getAction( menu.getButtonDown(Cam.getPos()) ) == ButtonOptions.MOVEFOXHOLE) {
				MenuBar.setTmpRequisition(requisition - FoxHole.REQCOST);
			} else {
				MenuBar.setTmpRequisition(requisition);
			}
		}
	}
	
	@Override
	public void draw(SpriteBatch Batch, Camera Cam, boolean CheckTargets, int CurrentStage)
	{
		checkReqCosts(Cam);
		
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			Squad c = s.next();
			
			boolean highlight = highlightSquad(c, Cam);
			c.draw(Batch, Cam, highlight);
		}
		
		drawDeployer(Batch, Cam);
		
		if (menuactive && selected != null) {
			Rectangle bbox = selected.getBoundingBox();
			
			if (CurrentStage == GameWorld.MOVESELECT) {
				menu.setPos( (int)(bbox.x + bbox.width/2), (int)(bbox.y - 48), Cam.getPos());
				menu.draw(Batch, Cam);
			} else if (CurrentStage == GameWorld.ATTACKSELECT) {
				offensemenu.setPos( (int)(bbox.x + bbox.width/2), (int)(bbox.y - 48), Cam.getPos());
				offensemenu.draw(Batch, Cam);
			}
		}
		
		if (moveactive && selected != null) {
			moveselect.draw(Batch, Cam);
		}
		
		if (targetpower && selected != null) {
			powerselect.draw(Batch, Cam);
		}
		
		if (profileactive && selected != null) {
			Profile.draw(Batch, selected, base.getLogo());
		}
		
		if (foxactive && selected != null) {
			foxselect.render(Batch, Cam);
		}
	}
}
