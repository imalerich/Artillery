package entity;

import java.util.Iterator;

import network.NetworkManager;
import network.Response;
import physics.CombatResolver;
import physics.GameWorld;
import terrain.Terrain;
import ui.FoxHoleMenu;
import arsenal.Armament;
import objects.RadioTower;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.MilitaryBase;

public class RemoteArmy extends Army
{
	public RemoteArmy(GameWorld World, MilitaryBase Base, Terrain Ter, NetworkManager Network, int ID)
	{
		super();
		
		world = World;
		ter = Ter;
		network = Network;
		
		setBase(Base);
		setConnection(ID);
		
		stagecompleted = new boolean[GameWorld.STAGECOUNT];
		for (int i=0; i<GameWorld.STAGECOUNT; i++) {
			stagecompleted[i] = false;
		}
	}

	@Override
	public boolean isTargeting() 
	{
		return false;
	}

	@Override
	public boolean isStageCompleted(int Stage) 
	{
		if (Stage == GameWorld.ATTACKUPDATE) {
			return true;
		}
		
		return stagecompleted[Stage];
	}

	@Override
	public SelectionStack getTargetOptions() 
	{
		return null;
	}

	@Override
	public void setTargetSquad(Squad Target) 
	{
		//
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
	
	public void procMessage(Camera Cam, Response r)
	{
		// check for stage completion
		if (r.request.equals("MOVESELECT")) {
			world.forceStage(GameWorld.MOVESELECT);
			stagecompleted[GameWorld.MOVESELECT] = r.b0;
			return;
			
		} else if (r.request.equals("MOVEUPDATE")) {
			world.forceStage(GameWorld.MOVEUPDATE);
			stagecompleted[GameWorld.MOVEUPDATE] = r.b0;
			return;
			
		} else if (r.request.equals("ATTACKSELECT")) {
			world.forceStage(GameWorld.ATTACKSELECT);
			stagecompleted[GameWorld.ATTACKSELECT] = r.b0;
			return;
			
		}
		
		if (r.request.equals("SQUADMOVE")) {
			Squad s = getSquad(r.i0);
			if (s == null)
				return;
			
			s.setTargetX(r.i1);
			return;
			
		} else if (r.request.equals("SQUADSPAWNED")) {
			// make sure the same id is used across all clients
			int tmp = spawnUnit(r.i0, r.i2, r.b0);
			getSquad(tmp).setID(r.i1);
			return;
			
		}
		
		if (r.request.equals("TANKFIRING")) {
			Squad s = getSquad(r.i0);
			if (s == null)  {
				return;
			}
			
			s.setForward(r.b1);
			s.setFiringPrimary(r.b0);
			s.setBarrelAngle(r.f0);
			s.setPowerRatio(r.f1);
			s.getPrimary().setDataFromNetwork(r);
			
			return;
			
		} else if (r.request.equals("FLAMEFIRING")) {
			Squad s = getSquad(r.i0);
			if (s == null)  {
				return;
			}
			
			s.setForward(r.b1);
			s.setFiringPrimary(r.b0);
			s.getPrimary().setDataFromNetwork(r);
			return;
			
		} else if (r.request.equals("UNITTARGET")) {
			Squad s = getSquad(r.i0);
			if (s == null)
				return;
			
			Squad t = world.getRemoteArmy(r.i1).getSquad(r.i2);
			s.setTargetSquad(t);
			if (s.getPrimary() != null)
				s.getPrimary().setDataFromNetwork(r);
			
			if (t != null) {
				t.setAsTarget();
			}
			
			return;
			
		} else if (r.request.equals("UNITGRENADE")) {
			Squad s = getSquad(r.i0);
			if (s == null)
				return;
			
			s.setForward(r.b1);
			s.setFiringSecondary(r.b0);
			s.getSecondary().setAngle(r.f0);
			s.setPowerRatio(r.f1);
			s.getSecondary().setDataFromNetwork(r);;
			return;
			
		} else if (r.request.equals("UNITMORTAR")) {
			Squad s = getSquad(r.i0);
			if (s == null)
				return;
			
			s.setForward(r.b1);
			s.setFiringOffhand(r.b0);
			s.getOffhand().setAngle(r.f0);
			s.setPowerRatio(r.f1);
			s.getOffhand().setDataFromNetwork(r);
			return;
			
		} else if (r.request.equals("CANCELATTACK")) {
			Squad s = getSquad(r.squad);
			if (s == null)
				return;
			
			// selected squad actions were canceled
			s.setTargetSquad(null);
			s.setFiringSecondary(false);
			s.setFiringPrimary(false);
			s.setSwapState(false);
			return;
			
		}
		
		if (r.request.equals("UNITHEALTH")) {
			Squad s = getSquad(r.i0);
			if (s == null)
				return;
			Unit u = s.getUnit(r.i1);
			if (u == null)
				return;
			
			u.setHealth(r.f0);
			u.setMaxHealth(r.f1);
			return;
			
		} else if (r.request.equals("UNITPOSITION")) {
			Squad s = getSquad(r.i0);
			if (s == null)
				return;
			Unit u = s.getUnit(r.i1);
			if (u == null)
				return;
			
			u.getPos( new Vector2(r.f0, r.f1) );
			return;
			
		}
		
		if (r.request.equals("ADDFOX")) {
			Vector2 pos = new Vector2(r.f0, r.f1);
			FoxHoleMenu.cutRoom(ter, pos);
			world.addFoxHole(pos);
			checkForFoxOccupancy(Cam.getPos());
			return;
			
		} else if (r.request.equals("ADDBARRICADE")) {
			Vector2 pos = new Vector2(r.f0, r.f1);
			world.addTankBarrier(pos);
			return;
			
		} else if (r.request.equals("ADDMINE")) {
			world.addLandMine(r.f0, r.i0, r.i1);
			return;
			
		}
		
		if (r.request.equals("SWAPSTATE")) {
			Squad s = getSquad(r.squad);
			s.setSwapState(r.b0);
			
			return;
			
		} else if (r.request.equals("ADDTOWER")) {
			Squad s = getSquad(r.squad);
			Army a = world.getRemoteArmy(r.source);
			Vector2 v = new Vector2(r.f0, r.f1);
			RadioTower t = new RadioTower(world, v, r.i0);
			t.setTowerSquad(s);
			a.removeSquad(r.squad);
			a.addTower(t);

			return;

		}
	}

	@Override
	public void catchMessage(Response r) 
	{
		if (getConnection() == r.source)
			return;

		response.add(r);
	}
	
	@Override
	public void addCombatData(CombatResolver Resolver)
	{
		// uses the power modifier for projectiles
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			Squad squad = s.next();
			
			if (squad.doSwapState()) {
				squad.swapState();
				squad.setSwapState(false);
			}
			
			// add primary weapons to the combat resolver
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
			
			if (squad.isFiringOffhand() && squad.getOffhand().getType() == Armament.POINTTARGET) {
				Resolver.addMortar(squad);
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
		
		s = squads.iterator();
		while (s.hasNext()) {
			Squad squad = s.next();
			squad.setTargetSquad(null);
			squad.setFiringPrimary(false);
			squad.setFiringSecondary(false);
		}
	}

	@Override
	public void updateMoveSelect(Camera Cam) 
	{
		//
	}

	@Override
	public void updateAttackSelect(Camera Cam) 
	{
		//
	}

	@Override
	public void drawTargetPos(SpriteBatch Batch, Camera Cam)
	{
		//
	}

	@Override
	public void drawTargetSquad(SpriteBatch Batch, Camera Cam) 
	{
		//
	}

	@Override
	public boolean isMenuOpen() 
	{
		return false;
	}

	@Override
	public boolean updateTargetOptions(int Size) 
	{
		return false;
	}
	
	@Override
	public void initStage(Camera Cam, int NewStage)
	{
		if (NewStage == GameWorld.ATTACKSELECT) {
			checkForFoxOccupancy(Cam.getPos());
			
			Iterator<Squad> s = squads.iterator();
			while (s.hasNext())
				s.next().checkUnitFireDamage();
		}
		
		// set the new stage as not completed
		stagecompleted[NewStage] = false;
	}
	
	@Override
	public void addSquad(Squad Add)
	{
		// set the id for this squad
		Add.setID(squadid);
		Add.takesDirectDamage(false);
		squadid++;
		
		squads.add(Add);
	}

	@Override
	public void addFox(Vector2 Pos) 
	{
		//
	}

	@Override
	public void addBarricade(Vector2 Pos) 
	{
		//
	}

	@Override
	public void addRequisition(int Ammount, Vector2 Pos) 
	{
		//
	}

	@Override
	public void spendRequisition(int Ammount, Vector2 Pos) 
	{
		//
	}

	@Override
	public void setIsTankDead(boolean TankIsDead) 
	{
		//
	}
}
