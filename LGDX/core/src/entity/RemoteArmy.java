package entity;

import java.util.Vector;

import network.NetworkManager;
import network.Request;
import network.Response;
import physics.GameWorld;
import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Camera;
import com.mygdx.game.MilitaryBase;

public class RemoteArmy extends Army
{
	private static final int DELAY = 100;
	private double clock;
	
	public RemoteArmy(MilitaryBase Base, Terrain Ter, NetworkManager Network, int ID)
	{
		ter = Ter;
		base = Base;
		network = Network;
		squads = new Vector<Squad>();
		clock = 0.0;
		
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
		if (r.request.equals("ATTACKSTAGESTATUS")) {
			stagecompleted[GameWorld.ATTACKSELECT] = r.b;
			
		} else if (r.request.equals("MOVESTAGESTATUS")) {
			stagecompleted[GameWorld.MOVESELECT] = r.b;
			
		}
	}

	@Override
	public void UpdateMoveSelect(Camera Cam) 
	{
		if (!DoSendReq()) 
			return;
		
		Request r = new Request();
		r.req = "MOVESTAGESTATUS";
		r.dest = GetConnection();
		r.army = GetID();
		
		network.GetClient().sendTCP(r);
	}

	@Override
	public void UpdateAttackSelect(Camera Cam) 
	{
		if (!DoSendReq()) 
			return;
		
		Request r = new Request();
		r.req = "ATTACKSTAGESTATUS";
		r.dest = GetConnection();
		r.army = GetID();
		
		network.GetClient().sendTCP(r);
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
	
	public boolean DoSendReq()
	{
		clock += Gdx.graphics.getDeltaTime();
		if (clock > DELAY/1000f) {
			clock = 0.0;
			return true;
		} else return false;
	}
}
