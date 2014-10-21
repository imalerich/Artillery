package arsenal;

import com.badlogic.gdx.math.Vector2;

import entity.Squad;

public class Armament 
{
	// constants that define the two possible types of armaments
	public static final int UNITTARGET = 0;
	public static final int POINTTARGET = 1;
	
	// generic armament properties
	private int type;
	private int range;
	private int firerate;
	private int strength;
	private int speed;
	private float accuracy;
	
	// targeting mechanism for armaments
	private Squad target;
	private float angle;
	
	public Armament(int Type, int Range, int FireRate, int Strength, int Speed, float Accuracy)
	{
		type = Type;
		if (type != UNITTARGET || type != POINTTARGET)
			type = UNITTARGET;
		
		range = Range;
		firerate = FireRate;
		strength = Strength;
		speed = Speed;
		
		// accuracy is floaing point in the bounds 0f -> 1f
		accuracy = Accuracy;
		accuracy = Math.min(accuracy, 1.0f);
		accuracy = Math.max(accuracy, 0.0f);
	}
	
	// set the target for this armement
	public void SetTarget(Squad Target)
	{
		target = Target;
	}
	
	public Squad GetTarget()
	{
		return target;
	}
	
	public void AddAngle(float Add)
	{
		angle += Add;
	}
	
	public Vector2 GetInitialVelocity()
	{
		return new Vector2( (float)Math.cos(angle)*speed, (float)Math.sin(angle)*speed );
	}
	
	// getter methods for the properties of this armament
	public int GetType()
	{
		return type;
	}
	
	public int GetRange()
	{
		return range;
	}
	
	public int GetFireRate()
	{
		return firerate;
	}
	
	public int GetStrength()
	{
		return strength;
	}
	
	public float GetAccuracy()
	{
		return accuracy;
	}
	
	public int GetSpeed()
	{
		return speed;
	}
}
