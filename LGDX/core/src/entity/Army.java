package entity;

import java.util.Iterator;
import java.util.Vector;

import particles.Particles;
import physics.CombatResolver;
import physics.GameWorld;
import physics.NullTank;
import terrain.Terrain;
import ui.UnitDeployer;
import arsenal.Armament;
import arsenal.Armor;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.MilitaryBase;

public class Army 
{
	protected MilitaryBase base;
	protected Vector<Squad> squads;
	protected Terrain ter;
	protected boolean squadspawned;
	
	public Army()
	{
		squads = new Vector<Squad>();
	}
	
	public Army(MilitaryBase Base, Terrain Ter)
	{
		ter = Ter;
		base = Base;
		squads = new Vector<Squad>();
	}
	
	public boolean IsTargeting()
	{
		return false;
	}
	
	public boolean IsStageCompleted(int Stage)
	{
		return true;
	}
	
	public boolean hasSpawnedSquad()
	{
		return squadspawned;
	}
	
	public void setSpawnedSquad(boolean Set)
	{
		squadspawned = Set;
	}
	
	public void InitStage(int NewStage)
	{
		// set the each squad as not firing
		if (NewStage == GameWorld.MOVESELECT) {
			Iterator<Squad> s = squads.iterator();
			while (s.hasNext()) {
				Squad squad = s.next();
				squad.SetFiring(false);
				squad.SetTargetSquad(null);
			}
		}
	}
	
	public void CheckForDeaths(Camera Cam, Vector<NullTank> Deceased, Particles Part)
	{
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			Squad squad = s.next();
			squad.CheckAlive(Cam.GetPos(), Deceased, Part);
			
			// check if the squad no longer has any surviving members
			if (squad.GetUnitCount() == 0) {
				s.remove();
			}
		}
	}
	
	public void SpawnUnit(int UnitType, int Count, Camera Cam, int Speed)
	{
		int offset = 76;
		if (UnitType == UnitDeployer.STEALTHOPS)
			offset = 192;
		else if (UnitType == UnitDeployer.SPECOPS)
			offset = 306;
		
		Squad s = new Squad(ter);
		s.SetTargetX((int)base.GetPos().x+offset);
		int spacing = s.GetSquadSpacing();
		AddSquad(s);
		
		for (int i=0; i<Count; i++)
		{
			Vector2 pos = new Vector2(base.GetPos().x+offset + i*spacing, 0);
			
			switch (UnitType)
			{
			case UnitDeployer.GUNMAN:	
				s.AddUnit( new Gunman(ter, pos, Speed), Cam);
				s.SetArmament( new Armament(Armament.UNITTARGET, 256, 3, 2, 800, 0.8f));
				s.SetArmor( new Armor(10, 5));
				break;
				
			case UnitDeployer.STEALTHOPS:
				s.AddUnit( new StealthTroop(ter, pos, Speed), Cam);
				s.SetArmament( new Armament(Armament.UNITTARGET, 256, 2, 3, 800, 0.7f));
				s.SetArmor( new Armor(10, 5));
				break;
				
			case UnitDeployer.SPECOPS:
				s.AddUnit( new SpecOps(ter, pos, Speed), Cam);
				s.SetArmament( new Armament(Armament.UNITTARGET, 256, 1, 6, 800, 0.95f));
				s.SetArmor( new Armor(10, 5));
				break;
				
			default:
				break;
			}
		}
	}
	
	public void AddCombatData(CombatResolver Resolver, Particles Particle)
	{
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			Squad squad = s.next();
			
			// add each squad and its target to the combat resolver
			if (squad.GetTargetSquad() != null && 
					squad.GetArmament().GetType() == Armament.UNITTARGET) {
				Resolver.AddConflict(Particle, squad, squad.GetTargetSquad());
			} else if (squad.IsFiring() && squad.GetArmament().GetType() == Armament.POINTTARGET) {
				Resolver.AddProjectile(Particle, squad, 1f);
			}
		}
	}
	
	public boolean UpdateTargetOptions(int Size)
	{
		return false;
	}
	
	public SelectionStack GetTargetOptions()
	{
		return null;
	}
	
	public void SetTargetSquad(Squad Target)
	{
		//
	}
	
	public void AddSquad(Squad Add)
	{
		squads.add(Add);
	}
	
	public void UpdateMove(Camera Cam)
	{
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext())
			s.next().Update(Cam.GetPos());
	}
	
	public void UpdateMoveSelect(Camera Cam)
	{
		//
	}
	
	public void UpdateAttackSelect(Camera Cam)
	{
		//
	}
	
	public int GetMouseOverCount(Camera Cam)
	{
		int count = 0;
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			if (s.next().IsMouseOver(Cam.GetPos()))
				count++;
		}
		
		return count;
	}
	
	public void GetMouseOver(SelectionStack Stack, Camera Cam)
	{
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			Squad squad = s.next();
			
			if (squad.IsMouseOver(Cam.GetPos())) {
				Stack.AddSquadOver(squad);
			}
		}
	}
	
	public boolean IsMenuOpen()
	{
		return false;
	}
	
	public void DrawBase(SpriteBatch Batch, Camera Cam)
	{
		base.Draw(Batch, Cam);
	}
	
	public void DrawBaseLogo(SpriteBatch Batch, Camera Cam)
	{
		base.DrawLogo(Batch, Cam);
	}
	
	public void DrawView(Camera Cam)
	{
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) 
			s.next().DrawView(Cam);
		
		base.DrawView(Cam);
	}
	
	public void DrawTargetPos(SpriteBatch Batch, Camera Cam)
	{
		//
	}
	
	public void DrawTargetSquad(SpriteBatch Batch, Camera Cam)
	{
		//
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam, boolean CheckTargets, int CurrentStage) 
	{
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			Squad squad = s.next();
			squad.Draw(Batch, Cam, false);
		}
	}
}
