package entity;

import java.util.Iterator;
import java.util.Vector;

import network.NetworkManager;
import network.Response;
import physics.CombatResolver;
import physics.GameWorld;
import terrain.Terrain;
import ui.FoxHoleMenu;
import arsenal.Armament;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.MilitaryBase;

public class RemoteArmy extends Army
{
	private Vector<Response> response;
	private float powerlevel;
	
	public RemoteArmy(GameWorld World, MilitaryBase Base, Terrain Ter, NetworkManager Network, int ID)
	{
		response = new Vector<Response>();
		world = World;
		ter = Ter;
		base = Base;
		network = Network;
		squads = new Vector<Squad>();
		
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
	public void updateThreads() 
	{
		// process each message
		for (int i=0; i<response.size(); i++) {
			procMessage(response.get(i));
		}
		
		response.clear();
	}
	
	public void procMessage(Response r)
	{
		// check for stage completion
		if (r.request.equals("MOVESELECT")) {
			stagecompleted[GameWorld.MOVESELECT] = r.b0;
			return;
			
		} else if (r.request.equals("MOVEUPDATE")) {
			stagecompleted[GameWorld.MOVEUPDATE] = r.b0;
			return;
			
		} else if (r.request.equals("ATTACKSELECT")) {
			stagecompleted[GameWorld.ATTACKSELECT] = r.b0;
			return;
			
		}
		
		if (r.request.equals("SQUADMOVE")) {
			int id = r.i0;
			int targetx = r.i1;
			getSquad(id).setTargetX(targetx);
			return;
			
		} else if (r.request.equals("SQUADSPAWNED")) {
			// make sure the same id is used across all clients
			int tmp = spawnUnit(r.i0);
			getSquad(tmp).setID(r.i1);
			return;
			
		}
		
		if (r.request.equals("TANKFIRING")) {
			Squad s = getSquad(r.i0);
			s.setForward(r.b1);
			s.setFiring(r.b0);
			s.setBarrelAngle(r.f0);
			powerlevel = r.f1;
			return;
			
		} else if (r.request.equals("UNITTARGET")) {
			Squad s = getSquad(r.i0);
			Squad t = world.getRemoteArmy(r.i1).getSquad(r.i2);
			s.setTargetSquad(t);
			
			if (t != null) {
				t.setAsTarget();
			}
			
			return;
		}
		
		if (r.request.equals("UNITHEALTH")) {
			Unit u = getSquad(r.i0).getUnit(r.i1);
			u.setHealth(r.f0);
			u.setMaxHealth(r.f1);
			return;
			
		} else if (r.request.equals("UNITPOSITION")) {
			Unit u = getSquad(r.i0).getUnit(r.i1);
			u.getPos( new Vector2(r.f0, r.f1) );
			return;
			
		}
		
		if (r.request.equals("ADDFOX")) {
			System.out.println("r.f1: " + r.f1);
			Vector2 pos = new Vector2(r.f0, r.f1);
			FoxHoleMenu.cutRoom(ter, pos);
			world.addFoxHole(pos);
			System.out.println("Pos.y: " + pos.y);
		}
	}

	@Override
	public void catchMessage(Response r) 
	{
		response.add(r);
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
				Resolver.addProjectile(squad, powerlevel,
						squad.getArmament().getStrength());
			}
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
	public void initStage(int NewStage)
	{
		super.initStage(NewStage);
		
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
}
