package entity;

import java.util.Iterator;

import network.NetworkManager;
import network.Response;
import objects.FoxHole;
import objects.TankBarrier;
import physics.CombatResolver;
import physics.GameWorld;
import terrain.Terrain;
import ui.Button;
import ui.ButtonOptions;
import ui.FoxHoleMenu;
import ui.MenuBar;
import ui.OutpostFlag;
import ui.PointSelect;
import ui.PowerButtons;
import ui.Profile;
import ui.SpawnIndicator;
import ui.TankBarrierMenu;
import ui.UnitDeployer;
import arsenal.Armament;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Cursor;
import com.mygdx.game.Game;
import com.mygdx.game.MilitaryBase;

import config.SquadConfigurations;

public class UserArmy extends Army
{
	private SpawnIndicator spawn;
	private SelectionStack optionstack;
	private SelectionStack targetstack;
	private int prevOptionStackSize;
	private int prevTargetStackSize;
	
	private boolean menuactive = false;
	private boolean menurelease = false;
	private ButtonOptions menu;
	private ButtonOptions offensemenu;
	private FoxHoleMenu foxselect;
	private TankBarrierMenu barrierselect;
	
	private int prevdeployi;
	private PointSelect moveselect;
	private PowerButtons powerselect;
	
	private boolean moveactive = false;
	private boolean foxactive = false;
	private boolean barricadeactive = false;
	private boolean profileactive = false;
	private boolean targetenemies = false;
	private boolean targetpoint = false;
	private boolean targetside = false;
	private boolean targetpower = false;
	private boolean grenade = false;
	private boolean selecttower = false;
	private boolean selectspawnpoint = false;
	private int spawnindex = -1;
	
	private Squad selected; // the currently selected squad, or null
	
	public UserArmy(GameWorld World, MilitaryBase Base, Terrain Ter, NetworkManager Network, int Connection)
	{
		super();
		
		world = World;
		ter = Ter;
		network = Network;
		
		setBase(Base);
		setConnection(Connection);
		
		UnitDeployer.setPos(base.getPos());
		setDeployBBox();
		
		optionstack = new SelectionStack();
		targetstack = new SelectionStack();
		prevOptionStackSize = 0;
		prevTargetStackSize = 0;
		
		menu = new ButtonOptions(0, 04);
		menu.addGlyph(Button.MOVE);
		menu.addGlyph(Button.MOVEFOXHOLE);
		menu.addGlyph(Button.MOVETANKTRAP);
		menu.addGlyph(Button.TOWER);
		menu.addGlyph(Button.UPGRADE);
		menu.addGlyph(Button.STOP);
		
		offensemenu = new ButtonOptions(0, 0);
		offensemenu.addGlyph(Button.GRENADEL);
		offensemenu.addGlyph(Button.GRENADER);
		offensemenu.addGlyph(Button.ATTACK);
		offensemenu.addGlyph(Button.STOP);
		
		foxselect = new FoxHoleMenu(Ter);
		barrierselect = new TankBarrierMenu(Ter);
		
		moveselect = new PointSelect(Ter);
		powerselect = new PowerButtons();
		
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
		response.add(r);
	}
	
	public void procMessage(Camera Cam, Response r)
	{
		//
	}
	
	@Override
	public void addRequisition(int Ammount, Vector2 Pos)
	{
		if (requisition == 0)
			return;
		
		requisition += Ammount;
		world.addReqIndicator(Pos, Ammount);
	}
	
	@Override
	public void spendRequisition(int Ammount, Vector2 Pos)
	{
		if (requisition == 0)
			return;
		
		requisition -= Ammount;
		world.addReqIndicator(Pos, -Ammount);
	}
	
	public boolean checkOutpostFlags()
	{
		return selecttower;
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
			if (squad.getTargetSquad() != null && squad.getPrimary().getType() == Armament.UNITTARGET) {
				Resolver.addConflict(squad, squad.getTargetSquad());
				continue;
				
			} else if (squad.isFiringPrimary() && squad.getPrimary().getType() == Armament.POINTTARGET) {
				Resolver.addMissile(squad);
				continue;
				
			} else if (squad.isFiringPrimary() && squad.getPrimary().getType() == Armament.FLAMETARGET) {
				Resolver.addFlame(squad);
				continue;
			}
			
			// add secondary weapons to the combat resolver
			if (squad.getSecondary() != null && squad.isFiringSecondary()) {
				switch (squad.getSecondary().getType())
				{
				case Armament.POINTTARGET:
					Resolver.addGrenade(squad, squad.getSecondary());
					break;
					
				case Armament.UNITTARGET:
					break;
				
				case Armament.LANDMINE:
					squad.addLandMines(getWorld());
					break;
					
				case Armament.FLAMETARGET:
					break;
					
				}
				
				continue;
			}
		}
		
		// remove combat information from all squads
		targetstack.reset();

		s = squads.iterator();
		while (s.hasNext()) {
			Squad squad = s.next();
			squad.setTargetSquad(null);
			squad.setFiringPrimary(false);
			squad.setFiringSecondary(false);
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
		// process each message
		for (int i=0; i<response.size(); i++) {
			procMessage(Cam, response.get(i));
		}
		
		response.clear();
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
		if (NewStage == GameWorld.ATTACKSELECT) {
			checkForFoxOccupancy(Cam.getPos());
		} else if (NewStage == GameWorld.MOVESELECT) {
			requisition += getReqBonus();
			
			Iterator<Squad> s = squads.iterator();
			while (s.hasNext())
				s.next().checkUnitFireDamage();
		}
		
		// set the new stage as not completed
		stagecompleted[NewStage] = false;
	}
	
	public int getReqBonus()
	{
		int reqbonus = 0;
		
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			Squad squad = s.next();
			reqbonus += squad.getReqBonus();
			world.addReqIndicator(new Vector2(squad.getBBox().x + squad.getBBox().width/2f, squad.getBBox().y + squad.getBBox().height), squad.getReqBonus());
		}
		
		return reqbonus;
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
		
		int selected = UnitDeployer.getSelected(Cam);
		if (UnitDeployer.contains(selected))
			optionstack.addBarracksOver();
		
		getMouseOver(optionstack, Cam, false);
		
		if (!IncludeDeployer)
			return;
		
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
		if (selectspawnpoint)
			updateSpawnPoint(Cam);
			
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
			
			spawnindex = selected;
			selectspawnpoint = true;
			spawn = new SpawnIndicator(ter, spawnindex, 0);
			spawn.setPos( Cursor.getMouseX(Cam.getPos()) + (int)Cam.getPos().x );
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
		
		if (barricadeactive)
			updateBarricade(Cam);
		
		if (selecttower)
			updateTowerSelect(Cam);
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
		
		else if (targetside)
			updateTargetSide(Cam);
		
		else if (targetpower)
			updateTargetPower(Cam);
		
		else if (grenade)
			updateTargetGrenades(Cam);
	}
	
	private void updateButtons(Vector2 Campos)
	{
		// DO NOT UPDATE THE MENU IF NO SQUAD IS SELECTED
		if (selected == null) {
			menuactive = false;
			return;
		}
		
		if (selected.getClassification() == Classification.TANK || 
				selected.getClassification() == Classification.TOWER) {
			menu.removeGlyph(Button.MOVEFOXHOLE);
			menu.removeGlyph(Button.MOVETANKTRAP);
			menu.removeGlyph(Button.TOWER);
			
		} else if (selected.getClassification() == Classification.STEALTHOPS) {
			// stealth troops cannot place fox holes or place tank traps
			menu.addGlyph(Button.MOVETANKTRAP);
			menu.removeGlyph(Button.MOVEFOXHOLE);
			
			menu.addGlyph(Button.TOWER);
			
		} else if (selected.getClassification() == Classification.GUNMAN ||
				selected.getClassification() == Classification.SPECOPS) {
			// gun men and specops cannot place tank traps
			menu.removeGlyph(Button.MOVETANKTRAP);
			
			menu.addGlyph(Button.MOVEFOXHOLE);
			menu.addGlyph(Button.TOWER);
			
		}
		
		if (!selected.canMove()) {
			menu.removeGlyph(Button.MOVE);
		} else {
			menu.addGlyph(Button.MOVE);
		}
		
		if (Cursor.isButtonJustPressed(Cursor.RIGHT)) {
			menuactive = false;
			menurelease  = false;
			menu.resetClock();
		}
		
		if (!Cursor.isButtonPressed(Cursor.LEFT))
			menurelease = true;
		
		Button event = Button.NULL;
		if (Cursor.isButtonJustReleased(Cursor.LEFT))
			event = menu.getAction( menu.getButtonDown(Campos) );
		
		switch (event)
		{
		case STOP:
			// leave the menu
			checkSelectedStop();
			
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

		case MOVE:
			// set the movement
			checkSelectedStop();
			moveactive = true;
			moveselect.setPos((int)selected.getBoundingBox().x, (int)selected.getBoundingBox().width);
			moveselect.setMaxDist(selected.getMoveDist());

			// leave the menu
			menuactive = false;
			menurelease = false;
			menu.resetClock();
			break;
			
		case MOVEFOXHOLE:
			checkSelectedStop();
			if (requisition - FoxHole.REQCOST >= 0) {
				foxselect.setSelected(selected);
				foxactive = true;
			}
			
			// leave the menu
			menuactive = false;
			menurelease = false;
			menu.resetClock();
			break;
			
		case MOVETANKTRAP:
			checkSelectedStop();
			if (requisition - TankBarrier.REQCOST >= 0) {
				barrierselect.setSelected(selected);
				barricadeactive = true;
			}
			
			// leave the menu
			menuactive = false;
			menurelease = false;
			menu.resetClock();
			break;
			
		case TOWER:
			checkSelectedStop();
			
			if (requisition - OutpostFlag.REQCOST >= 0) {
				selecttower = true;
			}
			
			// leave the menu
			menuactive = false;
			menurelease = false;
			menu.resetClock();
			break;
			
		case UPGRADE:
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
	
	private void checkSelectedStop()
	{
		if (selected == null)
			return;
		
		selected.setTargetX(-1);
		Vector2 p = new Vector2(selected.getBBox().x + selected.getBBox().width/2f, selected.getBBox().y + selected.getBBox().height);
		if (selected.doAddFox()) {
			selected.addFoxOnFinishMove(null, false);
			addRequisition(FoxHole.REQCOST, p);
		} 

		if (selected.doAddBarrier()) {
			selected.addBarrierOnFinishedMove(null, false);
			addRequisition(TankBarrier.REQCOST, p);
		}

		if (selected.doAddTower()) {
			selected.addOutpostOnFinishedMove(null, false);
			addRequisition(OutpostFlag.REQCOST, p);
		}
	}
	
	private void updateSpawnPoint(Camera Cam)
	{
		if (!selectspawnpoint || spawnindex == -1 || spawn == null)
			return;
		
		// get the world position of the world
		int xpos = Cursor.getMouseX(Cam.getPos()) + (int)Cam.getPos().x;
		spawn.setPos(xpos);
		spawn.setValid( base.isPointInBounds(spawn.getPos()) );
		
		int direction = GameWorld.getDirection((int)base.getMidX(), 0f, spawn.getPos().x, 0f);
		spawn.setForward(true);
		if (direction < 0)
			spawn.setForward(false);
		
		if (Cursor.isButtonJustPressed(Cursor.RIGHT)) {
			selectspawnpoint = false;
			spawnindex = -1;
			spawn = null;
		}
	
		if (Cursor.isButtonJustPressed(Cursor.LEFT)) {
			// if the position is valid
			if (spawn.isValid()) {
				// go ahead and spawn the unit
				int postcost = reqPostDeploy(spawnindex);
				requisition = postcost;
				int id = spawnUnit(spawnindex, xpos, spawn.isForward());

				// tell all clients of the newly spawned squad
				Response r = new Response();
				r.source = getConnection();
				r.request = "SQUADSPAWNED";
				r.i0 = spawnindex;
				r.i1 = id;
				r.i2 = xpos;
				r.b0 = spawn.isForward();

				network.getClient().sendTCP(r);
			}
			
			// leave the menu
			selectspawnpoint = false;
			spawnindex = -1;
			spawn = null;
		}
	}
	
	private void updateFox(Camera Cam)
	{
		if (selected == null) {
			return;
		}
		
		if (Cursor.isButtonPressed(Cursor.RIGHT) || selected == null) {
			foxactive = false;
			return;
		}
		
		if (Cursor.isButtonJustPressed(Cursor.LEFT)) {
			if (foxselect.isPosValid()) {
				spendRequisition(FoxHole.REQCOST, new Vector2(foxselect.getPos().x + FoxHole.FOXHOLE.getWidth()/2f, 
						foxselect.getPos().y + FoxHole.FOXHOLE.getHeight()));
				foxselect.setSelectedTarget(selected);
			}
			
			foxactive = false;
			return;
		}
		
		foxselect.update(Cam);
	}
	
	private void updateBarricade(Camera Cam)
	{
		if (Cursor.isButtonPressed(Cursor.RIGHT) || selected == null) {
			barricadeactive = false;
			return;
		}
		
		if (Cursor.isButtonJustPressed(Cursor.LEFT)) {
			
			barrierselect.setSelectedTarget(selected);
			float xpos = barrierselect.getBBox().x;
			float ypos = Game.WORLDH - ter.getHeight((int)xpos) + TankBarrier.TANKBARRIER.getHeight();
			spendRequisition( TankBarrier.REQCOST, new Vector2(xpos, ypos) );
			
			barricadeactive = false;
			return;
		}
		
		barrierselect.update(Cam);
	}
	
	private void updateTowerSelect(Camera Cam)
	{
		if (Cursor.isButtonPressed(Cursor.RIGHT) || selected == null) {
			selecttower = false;
			return;
		}
		
		if (Cursor.isButtonJustPressed(Cursor.LEFT)) {
			OutpostFlag f = world.getFlagOver();
			
			if (f != null) {
				selected.setTargetX( (int)(f.getBBox().x + f.getBBox().width/2) - (int)(selected.getBBox().width/2) );
				
				int direction = GameWorld.getDirection(selected.getBBox().x + selected.getBBox().width/2, 0f, 
						selected.getTargetX() + selected.getBBox().width/2, 0f);
				if (direction > 0)
					selected.setTargetX( selected.getTargetX() + (int)selected.getWidth()*2);
				
				selected.addOutpostOnFinishedMove(f, true);
				spendRequisition( OutpostFlag.REQCOST, new Vector2(f.getBBox().x + f.getBBox().width/2f, 
						f.getBBox().y + f.getBBox().height) );
				
				// tell all clients which squad is moving
				Response r = new Response();
				r.request = "SQUADMOVE";
				r.i0 = selected.getID();
				r.i1 = moveselect.getTargetX();
				r.source = getConnection();

				network.getClient().sendTCP(r);
			}
			
			selecttower = false;
			return;
		}
	}
	
	public void addFox(Vector2 Pos)
	{
		FoxHoleMenu.cutRoom(ter, Pos);
		world.addFoxHole(Pos);
		
		// inform all clients of the added fox hole
		Response r = new Response();
		r.source = getConnection();
		r.request = "ADDFOX";
		r.f0 = Pos.x;
		r.f1 = Pos.y;

		network.getClient().sendTCP(r);
	}
	
	public void addBarricade(Vector2 Pos)
	{
		world.addTankBarrier(Pos);
		
		// inform all clients of the added barricade
		Response r = new Response();
		r.source = getConnection();
		r.request = "ADDBARRICADE";
		r.f0 = Pos.x;
		r.f1 = Pos.y;
		
		network.getClient().sendTCP(r);
	}
	
	private void updateOffenseButtons(Vector2 Campos)
	{
		// DO NOT UPDATE THE MENU IF NO SQUAD IS SELECTED
		if (selected == null) {
			menuactive = false;
			return;
		}
		
		if (selected.getClassification() == Classification.TANK) {
			offensemenu.removeGlyph(Button.LANDMINE);
			offensemenu.removeGlyph(Button.GRENADEL);
			offensemenu.removeGlyph(Button.GRENADER);
			
		} else if (selected.getClassification() == Classification.TOWER){
			offensemenu.removeGlyph(Button.LANDMINE);
			offensemenu.removeGlyph(Button.GRENADEL);
			offensemenu.removeGlyph(Button.GRENADER);
			
		} else if (selected.getClassification() == Classification.GUNMAN || 
				selected.getClassification() == Classification.SPECOPS) {
			offensemenu.removeGlyph(Button.LANDMINE);
			offensemenu.addGlyph(Button.GRENADEL);
			offensemenu.addGlyph(Button.GRENADER);
			
		} else if (selected.getClassification() == Classification.STEALTHOPS) {
			offensemenu.addGlyph(Button.LANDMINE);
			offensemenu.removeGlyph(Button.GRENADEL);
			offensemenu.removeGlyph(Button.GRENADER);
		}
		
		if (Cursor.isButtonJustPressed(Cursor.RIGHT)) {
			menuactive = false;
			menurelease  = false;
			offensemenu.resetClock();
		}
		
		if (!Cursor.isButtonPressed(Cursor.LEFT))
			menurelease = true;
		
		Button event = Button.NULL;
		if (Cursor.isButtonJustReleased(Cursor.LEFT))
			event = offensemenu.getAction( offensemenu.getButtonDown(Campos) );
		
		switch (event)
		{
		case STOP:
			// leave the menu and do not select a squad
			selected.setTargetSquad(null);
			selected.setFiringSecondary(false);
			selected.setFiringPrimary(false);
			
			menuactive = false;
			menurelease = false;
			offensemenu.resetClock();
			break;
			
		case ATTACK:
			// leave the menu
			menuactive = false;
			menurelease = false;
			offensemenu.resetClock();
			
			selected.setTargetSquad(null);
			selected.setFiringPrimary(false);
			selected.setFiringSecondary(false);
			
			if (selected.getPrimary().getType() == Armament.UNITTARGET) {
				targetenemies = true;
			} else if (selected.getPrimary().getType() == Armament.POINTTARGET) {
				targetpoint = true;
			} else if (selected.getPrimary().getType() == Armament.FLAMETARGET) {
				targetside = true;
			}
			
			break;
			
		case LANDMINE:
			selected.setFiringSecondary(true);
			
			// leave the menu
			menuactive = false;
			menurelease = false;
			offensemenu.resetClock();
			
			break;
			
		case GRENADEL:
			// leave the menu
			selected.setTargetSquad(null);
			menuactive = false;
			menurelease = false;
			offensemenu.resetClock();
			
			selected.setTargetSquad(null);
			selected.setFiringPrimary(false);
			selected.setFiringSecondary(false);
			selected.setForward(false);
			selected.getSecondary().setAngle(45);
			
			Vector2 pos = new Vector2(selected.getBBox().x-64, selected.getBBox().y + selected.getBBox().height/2);
			powerselect.setPos(pos);
			grenade = true;
			break;
			
		case GRENADER:
			// leave the menu
			menuactive = false;
			menurelease = false;
			offensemenu.resetClock();
			
			selected.setTargetSquad(null);
			selected.setFiringPrimary(false);
			selected.setFiringSecondary(false);
			selected.setForward(true);
			selected.getSecondary().setAngle(45);
			
			pos = new Vector2(selected.getBBox().x+selected.getBBox().width+64, selected.getBBox().y + selected.getBBox().height/2);
			powerselect.setPos(pos);
			grenade = true;
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
			selected.setFiringPrimary(true);
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
		
		selected.setFiringPrimary(false);
		selected.setFiringSecondary(false);
		selected.setBarrelAngle(theta);
	}
	
	private void updateTargetSide(Camera Cam)
	{
		// INVALID - do not select a side
		if (selected == null) {
			targetside = false;
			return;
		}
		
		// leave targeting do not select side
		if (Cursor.isButtonJustPressed(Cursor.RIGHT)) {
			targetside = false;
			return;
		}
		
		// leave the current unit selected and leave targeting for this squad
		if (Cursor.isButtonJustReleased(Cursor.LEFT)) {
			targetside = false;
			selected.setFiringPrimary(true);
			selected.setFiringSecondary(false);
			
			// send a message to all clients informing them who is firing and where
			Response r = new Response();
			r.source = getConnection();
			r.request = "FLAMEFIRING";
			r.i0 = selected.getID();
			r.b0 = selected.isFiringPrimary();
			r.b1 = selected.isForward();
			
			network.getClient().sendTCP(r);
			
			return;
		}
		
		// do not set the angle when the mouse is on the incorrect side of the selected unit
		float xpos = Cursor.getMouseX(Cam.getPos()) + Cam.getPos().x;
		float startx = selected.getBBox().x + selected.getBBox().width/2f;
		int direction = GameWorld.getDirection(startx, 0f, 
				xpos, 0f);
		if (direction != 1 && selected.isForward()) {
			selected.setForward(false);
		} else if (direction != -1 && !selected.isForward()) {
			selected.setForward(true);
		}
		
		selected.setFiringPrimary(false);
		selected.setFiringSecondary(false);
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
			selected.setFiringPrimary(false);
			selected.setFiringSecondary(false);
			targetpower = false;
			
			return;
		}
		
		// leave the current unit selected and leave targeting for this squad
		if (powerselect.doFire(Cam)) {
			selected.setPowerRatio(powerselect.getPower()/PowerButtons.MAXPOWER);
			selected.setFiringPrimary(true);
			selected.setFiringSecondary(false);
			targetpower = false;
			
			// send a message to all clients informing them who is firing and where
			Response r = new Response();
			r.source = getConnection();
			r.request = "TANKFIRING";
			r.i0 = selected.getID();
			r.b0 = selected.isFiringPrimary();
			r.b1 = selected.isForward();
			r.f0 = selected.getPrimary().getAngle();
			r.f1 = selected.getPowerRatio();
			
			network.getClient().sendTCP(r);
			
			return;
		}
		
		powerselect.update(Cam);
		MenuBar.setPowerLevel(powerselect.getPower(), PowerButtons.MAXPOWER);
	}
	
	private void updateTargetGrenades(Camera Cam)
	{
		// INVALID - do not select a point
		if (selected == null) {
			grenade = false;
			return;
		}
		
		// leave targeting, do not select a point
		if (Cursor.isButtonJustPressed(Cursor.RIGHT)) {
			selected.setFiringPrimary(false);
			selected.setFiringSecondary(false);
			grenade = false;
			
			return;
		}
		
		if (powerselect.doFire(Cam)) {
			selected.setPowerRatio(powerselect.getPower()/PowerButtons.MAXPOWER);
			selected.setFiringPrimary(false);
			selected.setFiringSecondary(true);
			grenade = false;
			
			// send a message to all clients informing them who is firing and where
			Response r = new Response();
			r.source = getConnection();
			r.request = "UNITGRENADE";
			r.i0 = selected.getID();
			r.b0 = selected.isFiringSecondary();
			r.b1 = selected.isForward();
			r.f0 = selected.getSecondary().getAngle();
			r.f1 = selected.getPowerRatio();
			
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
		return (menuactive || moveactive || profileactive || targetenemies || targetpoint || targetside || 
				targetpower || foxactive || grenade || barricadeactive || selecttower || selectspawnpoint);
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
		MenuBar.setRequisition(requisition);
		
		if (!profileactive)
			MenuBar.setTmpRequisition(requisition);
		
		// check for actions that cost requisition
		if (menuactive) {
			if (menu.getAction( menu.getButtonDown(Cam.getPos()) ) == Button.MOVEFOXHOLE) {
				MenuBar.setTmpRequisition(requisition - FoxHole.REQCOST);
				
			} else if (menu.getAction( menu.getButtonDown(Cam.getPos()) ) == Button.MOVETANKTRAP) {
				MenuBar.setTmpRequisition(requisition - TankBarrier.REQCOST);
				
			} else if (menu.getAction( menu.getButtonDown(Cam.getPos()) ) == Button.TOWER) {
				MenuBar.setTmpRequisition(requisition - OutpostFlag.REQCOST);
				
			} else {
				MenuBar.setTmpRequisition(requisition);
				
			}
		}
		
		if (foxactive)
			MenuBar.setTmpRequisition(requisition - FoxHole.REQCOST);
		else if (barricadeactive)
			MenuBar.setTmpRequisition(requisition - TankBarrier.REQCOST);
		else if (selecttower)
			MenuBar.setTmpRequisition(requisition - OutpostFlag.REQCOST);
	}
	
	@Override
	public void draw(SpriteBatch Batch, Camera Cam, boolean CheckTargets, int CurrentStage)
	{
		checkReqCosts(Cam);
		
		// draw each squad
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
		
		if ((grenade || targetpower) && selected != null) {
			powerselect.draw(Batch, Cam);
		}
		
		if (profileactive && selected != null) {
			Profile.draw(Batch, selected, base.getLogo());
		}
		
		if (foxactive && selected != null) {
			foxselect.render(Batch, Cam);
		}
		
		if (barricadeactive && selected != null) {
			barrierselect.render(Batch, Cam);
		}
		
		if (spawn != null) {
			spawn.draw(Batch, Cam);
		}
	}
}
