package entity;

import java.util.Vector;

import network.NetworkManager;
import network.Response;
import physics.GameWorld;
import terrain.Terrain;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Camera;
import com.mygdx.game.MilitaryBase;

public class RemoteArmy extends Army
{
	public RemoteArmy(MilitaryBase Base, Terrain Ter, NetworkManager Network, int ID)
	{
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
	public void ProcMessage(Response r) 
	{
		if (r.request.equals("MOVESELECT")) {
			stagecompleted[GameWorld.MOVESELECT] = r.b;
			
		} else if (r.request.equals("MOVEUPDATE")) {
			stagecompleted[GameWorld.MOVEUPDATE] = r.b;
			
		} else if (r.request.equals("ATTACKSELECT")) {
			stagecompleted[GameWorld.ATTACKSELECT] = r.b;
			
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
		// TODO Auto-generated method stub
	}

	@Override
	public void DrawTargetSquad(SpriteBatch Batch, Camera Cam) 
	{
		// TODO Auto-generated method stub
	}

	@Override
	public boolean IsMenuOpen() 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean UpdateTargetOptions(int Size) 
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void InitStage(int NewStage)
	{
		super.InitStage(NewStage);
		
		// set the new stage as not completed
		stagecompleted[NewStage] = false;
	}
}
