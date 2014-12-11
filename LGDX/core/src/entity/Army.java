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
	public abstract void updateThreads(Camera Cam);
	
	public abstract boolean isTargeting();
	
	public abstract boolean isStageCompleted(int Stage);
	
	public abstract SelectionStack getTargetOptions();
	
	public abstract void setTargetSquad(Squad Target);
	
	public abstract void updateMoveSelect(Camera Cam);
	
	public abstract void updateAttackSelect(Camera Cam);
	
	public abstract void drawTargetPos(SpriteBatch Batch, Camera Cam);
	
	public abstract void drawTargetSquad(SpriteBatch Batch, Camera Cam);
	
	public abstract boolean isMenuOpen();
	
	public abstract boolean updateTargetOptions(int Size);
	
	public abstract void catchMessage(Response r);
	
	public abstract void addFox(Vector2 Pos);
	
	public abstract void addBarricade(Vector2 Pos);
	
	public abstract void addCombatData(CombatResolver Resolver);
	
	public void setID(int ID)
	{
		id = ID;
	}
	
	public int getID()
	{
		return id;
	}
	
	public GameWorld getWorld()
	{
		return world;
	}
	
	public NetworkManager getNetwork()
	{
		return network;
	}
	
	public void setStageCompleted(int Stage, boolean State)
	{
		stagecompleted[Stage] = State;
	}
	
	public void setConnection(int ID)
	{
		connection = ID;
	}
	
	public int getConnection()
	{
		if (connection == -1) {
			System.err.println("Error: Invalid connection - using connection 0 instead.");
			return 0;
		}
		
		return connection;
	}
	
	public void procBlasts(Blast B)
	{
		// process all blasts on this army
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			s.next().procBlasts(B);
		}
	}
	
	public void initStage(Camera Cam, int NewStage)
	{
		// set the each squad as not firing
		if (NewStage == GameWorld.MOVESELECT) {
			Iterator<Squad> s = squads.iterator();
			while (s.hasNext()) {
				Squad squad = s.next();
				squad.setFiring(false);
				squad.setTargetSquad(null);
			}
		}
	}
	
	public void checkForDeaths(Camera Cam, Vector<NullTank> Deceased, Particles Part)
	{
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			Squad squad = s.next();
			squad.checkAlive(Cam.getPos(), Deceased, Part);
			
			// check if the squad no longer has any surviving members
			if (squad.getUnitCount() == 0) {
				s.remove();
			}
		}
	}
	
	public void checkForFoxOccupancy(Vector2 Campos)
	{
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			s.next().checkIfOccupiesFox(Campos);
		}
	}
	
	public int spawnUnit(int UnitType)
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
			c = SquadConfigurations.getConfiguration(SquadConfigurations.GUNMAN);
			break;
			
		case UnitDeployer.STEALTHOPS:
			c = SquadConfigurations.getConfiguration(SquadConfigurations.STEALTHOPS);
			break;
			
		case UnitDeployer.SPECOPS:
			c = SquadConfigurations.getConfiguration(SquadConfigurations.SPECOPS);
			break;
		}
		
		Squad s = new Squad(ter, c.maxmovedist, this);
		s.setTargetX((int)base.getPos().x+offset);
		
		int spacing = s.getSquadSpacing();
		addSquad(s);
		
		// set the armor and armament for the squad
		s.setPrimary(c.getFirstPrimary());
		s.setSecondary(c.getFirstSecondary());
		s.setArmor(c.getFirstArmor());
		
		for (int i=0; i<c.count; i++)
		{
			Vector2 pos = new Vector2(base.getPos().x+offset + i*spacing, 0);
			
			switch (UnitType)
			{
			case UnitDeployer.GUNMAN:	
				s.addUnit( new Gunman(Gunman.GUNMAN, ter, pos, c.speed, c.health));
				break;
				
			case UnitDeployer.STEALTHOPS:
				s.addUnit( new Gunman(Gunman.STEALTHTROOPS, ter, pos, c.speed, c.health));
				break;
				
			case UnitDeployer.SPECOPS:
				s.addUnit( new Gunman(Gunman.SPECOPS, ter, pos, c.speed, c.health));
				break;
				
			default:
				break;
			}
		}
		
		return s.getID();
	}
	
	public void addSquad(Squad Add)
	{
		// set the id for this squad
		Add.setID(squadid);
		squadid++;
		
		squads.add(Add);
	}
	
	public Squad getSquad(int ID)
	{
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			Squad squad = s.next();
			if (squad.getID() == ID) {
				return squad;
			}
		}
		
		System.err.println("Error: Squad not found at id: " + ID);
		return null;
	}
	
	public void updateMove(Camera Cam)
	{
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext())
			s.next().update(Cam.getPos());
	}
	
	public int getMouseOverCount(Camera Cam)
	{
		int count = 0;
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			if (s.next().isMouseOver(Cam.getPos()))
				count++;
		}
		
		return count;
	}
	
	public void getMouseOver(SelectionStack Stack, Camera Cam, boolean IgnoreFox)
	{
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			Squad squad = s.next();
			
			if (IgnoreFox && squad.isInFox()) {
				continue;
			}
			
			if (squad.isMouseOver(Cam.getPos())) {
				Stack.addSquadOver(squad);
			}
		}
	}
	
	public void drawBase(SpriteBatch Batch, Camera Cam)
	{
		base.draw(Batch, Cam);
	}
	
	public void drawBaseLogo(SpriteBatch Batch, Camera Cam)
	{
		base.drawLogo(Batch, Cam);
	}
	
	public void drawView(Camera Cam)
	{
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) 
			s.next().drawView(Cam);
		
		base.drawView(Cam);
	}
	
	public void draw(SpriteBatch Batch, Camera Cam, boolean CheckTargets, int CurrentStage) 
	{
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext()) {
			Squad squad = s.next();
			squad.draw(Batch, Cam, false);
		}
	}
}
