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
import com.mygdx.game.Game;
import com.mygdx.game.Camera;
import com.mygdx.game.MilitaryBase;

public class CompArmy extends Army {
	public CompArmy(GameWorld World, MilitaryBase Base, Terrain Ter, NetworkManager Network, int Connection)
	{
		super();

		world = World;
		ter = Ter;
		network = Network;

		setBase(Base);
		setConnection(Connection);

		stagecompleted = new boolean[GameWorld.STAGECOUNT];
		for (int i=0; i<GameWorld.STAGECOUNT; i++) {
			stagecompleted[i] = true;
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
		//
	}

	@Override
	public void catchMessage(Response r) 
	{
		if (getConnection() == r.source)
			return;

		System.out.println("Adding response to queue: " + r.request + '.');
		response.add(r);
	}
	
	@Override
	public void addCombatData(CombatResolver Resolver)
	{
		//
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

		// TODO - the stage will be set to uncomplete here
	}
	
	@Override
	public void addSquad(Squad Add)
	{
		// set the id for this squad
		Add.setID(squadid);
		Add.takesDirectDamage(!Game.NETWORKED);
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