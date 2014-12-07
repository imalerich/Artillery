package entity;

import java.util.Iterator;
import java.util.Vector;

import network.NetworkManager;
import network.Response;
import particles.Particles;
import physics.Blast;
import physics.CombatResolver;
import physics.GameWorld;
import physics.NullTank;
import terrain.Terrain;
import ui.UnitDeployer;
import arsenal.Armament;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.MilitaryBase;

import config.ConfigSettings;
import config.SquadConfigurations;

public abstract class Army 
{
	private int id = 0;
	private int connection = -1;
	protected int squadid = 0;
	
	protected GameWorld world;
	protected boolean[] stagecompleted;
	protected NetworkManager network;
	protected MilitaryBase base;
	protected Vector<Squad> squads;
	protected Terrain ter;
	protected int requisition = 500;
	
	/**
	 * Process methods from other threads.
	 */
	public abstract void UpdateThreads();
	
	public abstract boolean IsTargeting();
	
	public abstract boolean IsStageCompleted(int Stage);
	
	public abstract SelectionStack GetTargetOptions();
	
	public abstract void SetTargetSquad(Squad Target);
	
	public abstract void UpdateMoveSelect(Camera Cam);
	
	public abstract void UpdateAttackSelect(Camera Cam);
	
	public abstract void DrawTargetPos(SpriteBatch Batch, Camera Cam);
	
	public abstract void DrawTargetSquad(SpriteBatch Batch, Camera Cam);
	
	public abstract boolean IsMenuOpen();
	
	public abstract boolean UpdateTargetOptions(int Size);
	
	public abstract void CatchMessage(Response r);
	
	public abstract void AddFox(Vector2 Pos);
	
	public void SetID(int ID)
	{
		id = ID;
	}
	
	public int GetID()
	{
		return id;
	}
	
	public NetworkManager GetNetwork()
	{
		return network;
	}
	
	public void SetStageCompleted(int Stage, boolean State)
	{
		stagecompleted[Stage] = State;
	}
	
	public void SetConnection(int ID)
	{
		connection = ID;
	}
	
	public int GetConnection()
	{
		if (connection == -1) {
			System.err.println("Error: Invalid connection - using connection 0 instead.");
			return 0;
		}
		
		return connection;
	}
	
	public void ProcBlasts(Blast B)
	{
		// process all blasts on this army
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			s.next().ProcBlasts(B);
		}
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
	
	public int SpawnUnit(int UnitType)
	{
		int offset = 76;
		if (UnitType == UnitDeployer.STEALTHOPS)
			offset = 192;
		else if (UnitType == UnitDeployer.SPECOPS)
			offset = 306;
		
		// get the appropriate configuration settings
		ConfigSettings c = null;
		switch (UnitType)
		{
		case UnitDeployer.GUNMAN:
			c = SquadConfigurations.GetConfiguration(SquadConfigurations.GUNMAN);
			break;
			
		case UnitDeployer.STEALTHOPS:
			c = SquadConfigurations.GetConfiguration(SquadConfigurations.STEALTHOPS);
			break;
			
		case UnitDeployer.SPECOPS:
			c = SquadConfigurations.GetConfiguration(SquadConfigurations.SPECOPS);
			break;
		}
		
		Squad s = new Squad(ter, c.maxmovedist, this);
		s.SetTargetX((int)base.GetPos().x+offset);
		
		int spacing = s.GetSquadSpacing();
		AddSquad(s);
		
		// set the armor and armament for the squad
		s.SetArmament(c.GetFirstArmament());
		s.SetArmor(c.GetFirstArmor());
		
		for (int i=0; i<c.count; i++)
		{
			Vector2 pos = new Vector2(base.GetPos().x+offset + i*spacing, 0);
			
			switch (UnitType)
			{
			case UnitDeployer.GUNMAN:	
				s.AddUnit( new Gunman(Gunman.GUNMAN, ter, pos, c.speed, c.health));
				break;
				
			case UnitDeployer.STEALTHOPS:
				s.AddUnit( new Gunman(Gunman.STEALTHTROOPS, ter, pos, c.speed, c.health));
				break;
				
			case UnitDeployer.SPECOPS:
				s.AddUnit( new Gunman(Gunman.SPECOPS, ter, pos, c.speed, c.health));
				break;
				
			default:
				break;
			}
		}
		
		return s.GetID();
	}
	
	public void AddCombatData(CombatResolver Resolver)
	{
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			Squad squad = s.next();
			
			// add each squad and its target to the combat resolver
			if (squad.GetTargetSquad() != null && 
					squad.GetArmament().GetType() == Armament.UNITTARGET) {
				Resolver.AddConflict(squad, squad.GetTargetSquad());
			} else if (squad.IsFiring() && squad.GetArmament().GetType() == Armament.POINTTARGET) {
				Resolver.AddProjectile(squad, 1f, squad.GetArmament().GetStrength());
			}
		}	
	}
	
	public void AddSquad(Squad Add)
	{
		// set the id for this squad
		Add.SetID(squadid);
		squadid++;
		
		squads.add(Add);
	}
	
	public Squad GetSquad(int ID)
	{
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			Squad squad = s.next();
			if (squad.GetID() == ID) {
				return squad;
			}
		}
		
		System.err.println("Error: Squad not found at id: " + ID);
		return null;
	}
	
	public void UpdateMove(Camera Cam)
	{
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext())
			s.next().Update(Cam.GetPos());
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
	
	public void Draw(SpriteBatch Batch, Camera Cam, boolean CheckTargets, int CurrentStage) 
	{
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			Squad squad = s.next();
			squad.Draw(Batch, Cam, false);
		}
	}
}
