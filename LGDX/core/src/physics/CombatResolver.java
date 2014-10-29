package physics;

import java.util.Iterator;
import java.util.Vector;

import arsenal.Armament;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;

import entity.Squad;
import entity.Unit;

public class CombatResolver 
{
	public static final Color BULLETCOL = new Color(128/255f, 128/255f, 128/255f, 1f);
	public static final int BULLETDIMMENSIONS = 3;
	public static final float HALFWIDTH = BULLETDIMMENSIONS/2f;
	private static Texture bullet;
	
	private Vector<CombatPacket> combatqueue = new Vector<CombatPacket>();
	
	public static void Init()
	{
		if (bullet == null)
		{
			Pixmap tmp = new Pixmap(BULLETDIMMENSIONS, BULLETDIMMENSIONS, Pixmap.Format.RGB888);
			tmp.setColor(BULLETCOL);
			tmp.fill();
			bullet = new Texture(tmp);
			tmp.dispose();
		}
	}
	
	public static void Release()
	{
		if (bullet != null)
			bullet.dispose();
	}
	
	public void StartSimulation()
	{
		combatqueue.clear();
	}
		
	public void AddConflict(Squad Offense, Squad Defense)
	{
		Armament arms = Offense.GetArmament();
		
		int direction = GetDirection(Offense.GetBBox().x, Offense.GetBBox().width, Defense.GetBBox().x);
		
		Vector<Unit> u = Defense.GetUnits();
		int index = 0;
		if (direction == -1)
			index = u.size()-1;
		
		Iterator<Unit> i = Offense.GetUnits().iterator();
		while (i.hasNext())
		{
			// look for a target for this unit
			Unit offense = i.next();
			Unit defense = u.get(index);
			combatqueue.add( new CombatPacket(offense, defense, arms) );
			
			// increment the index
			if (direction == 1)
				index++;
			else index--;
			
			if (index == u.size()) {
				index = 0;
			} else if (index < 0) {
				index = u.size()-1;
			}
		}
	}
	
	public boolean IsSimulationCompleted()
	{
		boolean completed = true;
		Iterator<CombatPacket> i = combatqueue.iterator();
		while (i.hasNext()) 
		{
			if (!i.next().IsCompleted()) {
				completed = false;
			}
		}
		
		return completed;
	}
	
	public void UpdateSimulation()
	{
		Iterator<CombatPacket> i = combatqueue.iterator();
		while (i.hasNext()) {
			CombatPacket p = i.next();
			if (p.IsCompleted()) {
				continue;
			}
			
			p.UpdatePosition();
		}
	}
	
	public void DrawSimulation(SpriteBatch Batch, Camera Cam)
	{
		Iterator<CombatPacket> i = combatqueue.iterator();
		while (i.hasNext())
		{
			CombatPacket p = i.next();
			if (p.IsCompleted()) {
				continue;
			}
			
			Vector2 pos = p.GetPosition();
			Batch.draw(bullet, Cam.GetRenderX(pos.x), Cam.GetRenderY(pos.y));
		}
	}
	
	private int GetDirection(float StartX, float StartWidth, float TargetX)
	{
		// check the distance to the target in each direction
		float rdist = (Game.WORLDW-(StartX+StartWidth))+TargetX;
		if (TargetX > StartX+StartWidth)
			rdist = TargetX-(StartX+StartWidth);
		
		float ldist = StartX + (Game.WORLDW-TargetX);
		if (TargetX < StartX)
			ldist = (StartX-TargetX);
		
		if (rdist < ldist)
			return 1;
		else if (ldist < rdist)
			return -1;
		else
			return 0;
	}
}
