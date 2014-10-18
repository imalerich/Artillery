package entity;

import java.util.Iterator;
import java.util.Vector;

import terrain.Terrain;
import ui.UnitDeployer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.MilitaryBase;

public class Army 
{
	protected MilitaryBase base;
	protected Vector<Squad> squads;
	protected Terrain ter;
	
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
	
	public boolean IsStageCompleted(int Stage)
	{
		return true;
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
				break;
				
			case UnitDeployer.STEALTHOPS:
				s.AddUnit( new StealthTroop(ter, pos, Speed), Cam);
				break;
				
			case UnitDeployer.SPECOPS:
				s.AddUnit( new SpecOps(ter, pos, Speed), Cam);
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
	
	public void DrawTargets(SpriteBatch Batch, Camera Cam)
	{
		//
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam)
	{
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext())
			s.next().Draw(Batch, Cam, false);
	}
}
