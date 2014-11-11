package physics;

import java.util.Iterator;
import java.util.Vector;

import particles.Particles;
import terrain.Terrain;
import arsenal.Armament;
import arsenal.Armor;

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
	private int stage;
	
	public CombatResolver(Terrain Ter)
	{
		ter = Ter;
		stage = COMPLETED;
	}
	
	public void StartSimulation()
	{
		stage = UNITSTAGE;
		combatqueue.clear();
	}
	
	public void AddProjectile(Particles Particle, Squad Offense, float Power)
	{
		// calculate the starting velocity
		Armament arms = Offense.GetArmament();
		double theta = Math.toRadians( Offense.GetBarrelAngle() );
		Vector2 vel = new Vector2((float)Math.cos(theta) * arms.GetSpeed() * Power, 
				(float)Math.sin(theta) * arms.GetSpeed() * Power);
		
		// calculate the starting position
		Vector2 pos = new Vector2(Offense.GetBBox().x, Offense.GetBBox().y);
		Vector2 barrelsrc = Offense.GetBarrelSrc();
		pos.x += barrelsrc.x;
		pos.y += barrelsrc.y;
		
		// check if the projectile should be fired to the left
		if (!Offense.IsForward()) {
			vel.x = -vel.x;
		}
		
		// add the projectile
		projectilequeue.add( new Missile(ter, Particle, pos, vel) );
	}
		
	public void AddConflict(Particles Particle, Squad Offense, Squad Defense)
	{
		Armament arms = Offense.GetArmament();
		Armor armor = Defense.GetArmor();
		Vector<Unit> u = Defense.GetUnits();
		int index = 0;
		
		int c = 0;
		Iterator<Unit> i = Offense.GetUnits().iterator();
		while (i.hasNext())
		{
			// look for a target for this unit
			Unit offense = i.next();
			Unit defense = u.get(index);

			float offset = 0.015f * c;
			
			for (int k=0; k<arms.GetFireRate(); k++) {
				// for each round a second apart
				combatqueue.add( new CombatPacket(ter, Particle, offense, defense, arms, armor, 2*k, offset) );
			}
			
			// increment the index
			c++;
			index++;
			if (index >= u.size())
				index = 0;
		}
		
		// set the offense to have no target
		Offense.SetTargetSquad(null);
		Offense.SetFiring(false);
	}
	
	public boolean IsSimulationCompleted()
	{
		return stage == COMPLETED;
	}
	
	public void UpdateSimulation()
	{
		// update the current stage
		if (stage == UNITSTAGE) {
			UpdateUnitStage();
		} else if (stage == POINTSTAGE) {
			UpdatePointStage();
		}
	}
	
	public void DrawSimulation(SpriteBatch Batch, Camera Cam)
	{
		// draw the state of the current stage
		if (stage == UNITSTAGE) {
			DrawUnitStage(Batch, Cam);
		} else if (stage == POINTSTAGE) {
			DrawPointStage(Batch, Cam);
		}
		
		// check if we should move on to the next stage
		CheckNextStage();
	}
	
	private void CheckNextStage()
	{
		if (stage == UNITSTAGE) {
			Iterator<CombatPacket> i = combatqueue.iterator();
			while (i.hasNext()) 
			{
				if (!i.next().IsCompleted()) {
					return;
				}
			}
			
			stage = POINTSTAGE;
			return;
			
		} else if (stage == POINTSTAGE) {
			Iterator<Missile> i = projectilequeue.iterator();
			while (i.hasNext()) {
				if (!i.next().HasHit()) {
					return;
				}
			}
			
			// wait for the terrain to finish updating
			if (!ter.IsValid()) {
				return;
			}

			stage = COMPLETED;
			return;
		}
	}
	
	private void UpdateUnitStage()
	{
		Iterator<CombatPacket> i = combatqueue.iterator();
		while (i.hasNext()) {
			CombatPacket p = i.next();
			if (p.IsCompleted()) {
				continue;
			}
			
			p.Update();
		}
	}
	
	private void DrawUnitStage(SpriteBatch Batch, Camera Cam)
	{
		Iterator<CombatPacket> i = combatqueue.iterator();
		while (i.hasNext())
		{
			CombatPacket p = i.next();
			if (p.IsCompleted()) {
				continue;
			}
			
			p.Draw(Batch, Cam);
		}
	}
	
	private void UpdatePointStage()
	{
		Iterator<Missile> i = projectilequeue.iterator();
		while (i.hasNext()) {
			Missile m = i.next();
			if (m.HasHit()) {
				continue;
			}
			
			m.Update();
		}
	}
	
	private void DrawPointStage(SpriteBatch Batch, Camera Cam)
	{
		Iterator<Missile> i = projectilequeue.iterator();
		while (i.hasNext()) {
			Missile m = i.next();
			if (m.HasHit()) {
				continue;
			}
			
			m.Draw(Batch, Cam);;
		}
	}
}
