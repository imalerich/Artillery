package physics;

import java.util.Iterator;
import java.util.Vector;

import arsenal.Armament;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;

import entity.Squad;
import entity.Unit;

public class CombatResolver 
{
	private static final Color BULLETCOL = new Color(54/255f, 51/255f, 48/255f, 1f);
	private static final int BULLETDIMMENSIONS = 2;
	private static Texture bullet;
	
	private Vector<CombatPacket> combatqueue;
	private float time = 0.0f;
	
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
	
	public void ClearQueue()
	{
		combatqueue.clear();
	}
	
	private void EndSimulation()
	{
		time = 0.0f;
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
		
		if (!completed) {
			EndSimulation();
		}
		
		return completed;
	}
	
	public void UpdateSimulation()
	{
		time += Gdx.graphics.getDeltaTime();
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
			
			float sourcex = p.GetOffense().GetPos().x;
			float sourcey = p.GetOffense().GetPos().y;
			float destx = p.GetDefense().GetPos().x;
			float desty = p.GetDefense().GetPos().y;
			
			float xpos = sourcex + ((destx-sourcex)*time * p.GetArmament().GetSpeed());
			float ypos = sourcey + ((desty-sourcey)*time * p.GetArmament().GetSpeed());
			Batch.draw(bullet, Cam.GetRenderX(xpos), Cam.GetRenderY(ypos));
			
			if (IsXPosMet(sourcex, destx, xpos) && IsYPosMet(sourcey, desty, ypos)) {
				p.SetCompleted();
			}
		}
	}
	
	public boolean IsXPosMet(float SourceX, float DestX, float XPos)
	{
		if (XPos == DestX) {
			return true;
		} if (SourceX < DestX && XPos < SourceX) {
			return true;
		} else if (SourceX > DestX && XPos > SourceX) {
			return true;
		}
		
		return false;
	}
	
	public boolean IsYPosMet(float SourceY, float DestY, float YPos)
	{
		if (YPos == DestY) {
			return true;
		} else if (DestY < SourceY && YPos < DestY) {
			return true;
		} else if (DestY > SourceY && YPos > DestY) {
			return true;
		}
		
		return false;
	}
}
