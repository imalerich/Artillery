package entity;

import java.util.Iterator;
import java.util.Vector;

import terrain.Terrain;
import ui.UnitDeployer;
import arsenal.Armament;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Cursor;
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
	
	public void InitStage()
	{
		//
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
				s.SetArmament( new Armament(Armament.UNITTARGET, 256, 1, 10, 200, 0.8f));
				break;
				
			case UnitDeployer.STEALTHOPS:
				s.AddUnit( new StealthTroop(ter, pos, Speed), Cam);
				s.SetArmament( new Armament(Armament.UNITTARGET, 256, 1, 10, 200, 0.7f));
				break;
				
			case UnitDeployer.SPECOPS:
				s.AddUnit( new SpecOps(ter, pos, Speed), Cam);
				s.SetArmament( new Armament(Armament.UNITTARGET, 256, 1, 10, 200, 0.95f));
				break;
				
			default:
				break;
			}
		}
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
	
	public void DrawBase(SpriteBatch Batch, Camera Cam)
	{
		base.Draw(Batch, Cam);
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
			boolean mouseover = Cursor.IsMouseOver(squad.GetBBox(), Cam.GetPos());
			
			squad.Draw(Batch, Cam, false, CheckTargets && mouseover);
		}
	}
}
