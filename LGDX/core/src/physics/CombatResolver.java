package physics;

import java.util.Iterator;
import java.util.Vector;

import particles.Particles;
import terrain.Terrain;
import arsenal.Armament;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;

import entity.Squad;
import entity.Unit;

public class CombatResolver 
{
	private static final int UNITSTAGE = 0;
	private static final int POINTSTAGE = 1;
	private static final int COMPLETED = 2;
	
	private Vector<Missile> projectilequeue = new Vector<Missile>();
	private Vector<CombatPacket> combatqueue = new Vector<CombatPacket>();
	private Terrain ter;
	private Particles particles;
	private GameWorld gw;
	private int stage;
	
	public CombatResolver(GameWorld GW, Terrain Ter, Particles Particle)
	{
		ter = Ter;
		particles = Particle;
		gw = GW;
		stage = COMPLETED;
	}
	
	public void startSimulation()
	{
		stage = UNITSTAGE;
		combatqueue.clear();
		projectilequeue.clear();
	}
	
	public void addGrenade(Squad Offense, Armament Grenade)
	{
		Vector2 vel = getVel(Grenade, Offense, Offense.getPowerRatio());
		
		Iterator<Unit> u = Offense.getUnitIterator();
		while (u.hasNext()) {
			Unit unit = u.next();
			Vector2 pos = getPos(unit);
		
			projectilequeue.add( new Grenade(gw, ter, particles, pos, vel, Grenade.getStrength(), Offense.getArmy().getConnection()) );
		}
	}
	
	public void addMissile(Squad Offense)
	{
		float strength = Offense.getPrimary().getStrength();
		Vector2 vel = getVel(Offense.getPrimary(), Offense, Offense.getPowerRatio());
		
		Iterator<Unit> u = Offense.getUnitIterator();
		while (u.hasNext()) {
			Unit unit = u.next();
			Vector2 pos = getPos(unit);
			
			projectilequeue.add( new Missile(gw, ter, particles, pos, vel, strength, Offense.getArmy().getConnection()) );
		}
			
	}
	
	private Vector2 getVel(Armament Arms, Squad Offense, float Power)
	{
		// calculate the starting velocity
		double theta = Math.toRadians( Arms.getAngle() );
		Vector2 vel = new Vector2((float)Math.cos(theta) * Arms.getSpeed() * Power, 
				(float)Math.sin(theta) * Arms.getSpeed() * Power);
		
		// check if the projectile should be fired to the left
		if (!Offense.isForward()) {
			vel.x = -vel.x;
		}
		
		return vel;
	}
	
	public Vector2 getPos(Unit U)
	{
		Vector2 pos = new Vector2(U.getPos().x, U.getPos().y);
		Vector2 barrelsrc = U.getBarrelSrc();
		pos.x += barrelsrc.x;
		pos.y += barrelsrc.y;
		
		return pos;
	}
		
	public void addConflict(Squad Offense, Squad Defense)
	{
		Armament arms = Offense.getPrimary();
		Vector<Unit> u = Defense.getUnits();
		int index = 0;
		
		int c = 0;
		Iterator<Unit> i = Offense.getUnits().iterator();
		while (i.hasNext())
		{
			// look for a target for this unit
			Unit offense = i.next();
			Unit defense = u.get(index);

			float offset = 0.055f * c;
			
			for (int k=0; k<arms.getFireRate(); k++) {
				// for each round a second apart
				combatqueue.add( new CombatPacket(ter, particles, offense, defense, arms, 2*k, offset, Defense.isInFox()) );
			}
			
			// increment the index
			c++;
			index++;
			if (index >= u.size())
				index = 0;
		}
		
		// set the offense to have no target
		Offense.setTargetSquad(null);
		Offense.setFiring(false);
	}
	
	public boolean isSimulationCompleted()
	{
		return stage == COMPLETED;
	}
	
	public void updateSimulation(Camera Cam)
	{
		// update the current stage
		if (stage == UNITSTAGE) {
			updateUnitStage(Cam);
		} else if (stage == POINTSTAGE) {
			updatePointStage(Cam);
		}
		
		// check if we should move on to the next stage
		checkNextStage();
	}
	
	public void drawSimulation(SpriteBatch Batch, Camera Cam)
	{
		// draw the state of the current stage
		if (stage == UNITSTAGE) {
			drawUnitStage(Batch, Cam);
		} else if (stage == POINTSTAGE) {
			drawPointStage(Batch, Cam);
		}
	}
	
	private void checkNextStage()
	{
		if (stage == UNITSTAGE) {
			Iterator<CombatPacket> i = combatqueue.iterator();
			while (i.hasNext()) 
			{
				if (!i.next().isCompleted()) {
					return;
				}
			}
			
			stage = POINTSTAGE;
			return;
			
		} else if (stage == POINTSTAGE) {
			Iterator<Missile> i = projectilequeue.iterator();
			while (i.hasNext()) {
				if (!i.next().isCompleted()) {
					return;
				}
			}
			
			// wait for the terrain to finish updating
			if (!ter.isValid()) {
				return;
			}

			stage = COMPLETED;
			combatqueue.clear();
			projectilequeue.clear();
		
			return;
		}
	}
	
	private void updateUnitStage(Camera Cam)
	{
		Iterator<CombatPacket> i = combatqueue.iterator();
		while (i.hasNext()) {
			CombatPacket p = i.next();
			if (p.isCompleted()) {
				continue;
			}
			
			p.update(Cam);
		}
	}
	
	private void drawUnitStage(SpriteBatch Batch, Camera Cam)
	{
		Iterator<CombatPacket> i = combatqueue.iterator();
		while (i.hasNext())
		{
			CombatPacket p = i.next();
			if (p.isCompleted()) {
				continue;
			}
			
			p.draw(Batch, Cam);
		}
	}
	
	private void updatePointStage(Camera Cam)
	{
		Iterator<Missile> i = projectilequeue.iterator();
		while (i.hasNext()) {
			Missile m = i.next();
			if (m.isCompleted()) {
				continue;
			}
			
			m.update(Cam);
		}
	}
	
	private void drawPointStage(SpriteBatch Batch, Camera Cam)
	{
		Iterator<Missile> i = projectilequeue.iterator();
		while (i.hasNext()) {
			Missile m = i.next();
			if (m.hasHit()) {
				continue;
			}
			
			m.draw(Batch, Cam);;
		}
	}
}
