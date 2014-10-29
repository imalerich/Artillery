package physics;

import arsenal.Armament;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Game;

import entity.Unit;

public class CombatPacket 
{
	private final Unit offense;
	private final Unit defense;
	private final Armament arms;
	private boolean iscompleted;
	
	private Vector2 position;
	private Vector2 target;
	private Vector2 speed;
	
	private Rectangle targetBBox;
	
	public CombatPacket(Unit Offense, Unit Defense, Armament Arms)
	{
		offense = Offense;
		defense = Defense;
		arms = Arms;
		iscompleted = false;
		
		// set the initial position and the target position
		position = new Vector2(offense.GetPos().x + offense.GetWidth()/2f, 
				offense.GetPos().y + offense.GetHeight()/2f);
		target = new Vector2(defense.GetPos().x, defense.GetPos().y + defense.GetHeight()/2f);
		
		// calculate the speed in each direction
		speed = new Vector2(target);
		speed.x -= position.x;
		speed.y -= position.y;
		speed.nor();
		speed.x = Math.abs(speed.x * arms.GetSpeed());
		speed.y *= arms.GetSpeed();
		
		// set the target bounding box
		targetBBox = new Rectangle(target.x, target.y, defense.GetWidth(), defense.GetHeight());
	}
	
	public void UpdatePosition()
	{
		// check if this unit has reached his position
		if (targetBBox.contains(position.x + CombatResolver.HALFWIDTH, 
				position.y + CombatResolver.HALFWIDTH)) {
			iscompleted = true;
			return;
		}
			
		int direction = GetMoveDirection();
		position.x += (direction * speed.x * Gdx.graphics.getDeltaTime());
		position.y += (speed.y * Gdx.graphics.getDeltaTime());
		
		if (position.x < 0) {
			position.x += Game.WORLDW;
		} else if (position.x > Game.WORLDW) {
			position.x -= Game.WORLDW;
		}
		
		if (direction == 0) {
			iscompleted = true;
			return;
		}
	}
	
	public Unit GetOffense()
	{
		return offense;
	}
	
	public Unit GetDefense()
	{
		return defense;
	}
	
	public Vector2 GetPosition()
	{
		return position;
	}
	
	public Armament GetArmament()
	{
		return arms;
	}
	
	public void SetCompleted()
	{
		iscompleted = true;
	}
	
	public boolean IsCompleted()
	{
		return iscompleted;
	}
	
	private int GetMoveDirection()
	{
		int width = CombatResolver.BULLETDIMMENSIONS;


		// check the distance to the target in each direction
		float rdist = (Game.WORLDW-(position.x+width)) + target.x;
		if (target.x > position.x)
			rdist = target.x -(position.x);

		float ldist = position.x + (Game.WORLDW - target.x);
		if (target.x < position.x)
			ldist = (position.x - target.x);
		
		if (rdist < ldist) {
			return 1;
		} else if (ldist < rdist) {
			return -1;
		} else {
			return 0;
		}
	}
}
