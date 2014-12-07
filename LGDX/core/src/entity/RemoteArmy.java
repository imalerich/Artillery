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
		
		SetConnection(ID);
		
		stagecompleted = new boolean[GameWorld.STAGECOUNT];
		for (int i=0; i<GameWorld.STAGECOUNT; i++) {
			stagecompleted[i] = false;
		}
	}

	@Override
	public boolean IsTargeting() 
	{
		return false;
	}

	@Override
	public boolean IsStageCompleted(int Stage) 
	{
		if (Stage == GameWorld.ATTACKUPDATE) {
			return true;
		}
		
		return stagecompleted[Stage];
	}

	@Override
	public SelectionStack GetTargetOptions() 
	{
		return null;
	}

	@Override
	public void SetTargetSquad(Squad Target) 
	{
		//
	}
	
	@Override
	public void UpdateThreads() 
	{
		// process each message
		for (int i=0; i<response.size(); i++) {
			ProcMessage(response.get(i));
		}
		
		response.clear();
	}
	
	public void ProcMessage(Response r)
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
			GetSquad(id).SetTargetX(targetx);
			return;
			
		} else if (r.request.equals("SQUADSPAWNED")) {
			// make sure the same id is used across all clients
			int tmp = SpawnUnit(r.i0);
			GetSquad(tmp).SetID(r.i1);
			return;
			
		}
		
		if (r.request.equals("TANKFIRING")) {
			Squad s = GetSquad(r.i0);
			s.SetForward(r.b1);
			s.SetFiring(r.b0);
			s.SetBarrelAngle(r.f0);
			powerlevel = r.f1;
			return;
			
		} else if (r.request.equals("UNITTARGET")) {
			Squad s = GetSquad(r.i0);
			Squad t = world.GetRemoteArmy(r.i1).GetSquad(r.i2);
			s.SetTargetSquad(t);
			
			if (t != null) {
				t.SetAsTarget();
			}
			
			return;
		}
		
		if (r.request.equals("UNITHEALTH")) {
			Unit u = GetSquad(r.i0).GetUnit(r.i1);
			u.SetHealth(r.f0);
			u.SetMaxHealth(r.f1);
			return;
			
		} else if (r.request.equals("UNITPOSITION")) {
			Unit u = GetSquad(r.i0).GetUnit(r.i1);
			u.SetPos( new Vector2(r.f0, r.f1) );
			return;
			
		}
		
		if (r.request.equals("ADDFOX")) {
			Vector2 pos = new Vector2(r.f0, r.f1);
			FoxHoleMenu.CutRoom(ter, pos);
			world.AddFoxHole(pos);
		}
	}

	@Override
	public void CatchMessage(Response r) 
	{
		response.add(r);
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
				Resolver.AddProjectile(squad, powerlevel,
						squad.GetArmament().GetStrength());
			}
		}
	}

	@Override
	public void UpdateMoveSelect(Camera Cam) 
	{
		//
	}

	@Override
	public void UpdateAttackSelect(Camera Cam) 
	{
		//
	}

	@Override
	public void DrawTargetPos(SpriteBatch Batch, Camera Cam)
	{
		//
	}

	@Override
	public void DrawTargetSquad(SpriteBatch Batch, Camera Cam) 
	{
		//
	}

	@Override
	public boolean IsMenuOpen() 
	{
		return false;
	}

	@Override
	public boolean UpdateTargetOptions(int Size) 
	{
		return false;
	}
	
	@Override
	public void InitStage(int NewStage)
	{
		super.InitStage(NewStage);
		
		// set the new stage as not completed
		stagecompleted[NewStage] = false;
	}
	
	@Override
	public void AddSquad(Squad Add)
	{
		// set the id for this squad
		Add.SetID(squadid);
		Add.TakesDirectDamage(false);
		squadid++;
		
		squads.add(Add);
	}

	@Override
	public void AddFox(Vector2 Pos) 
	{
		//
	}
}
