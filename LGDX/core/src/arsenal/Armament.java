package arsenal;


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
	private float angle;
	
	public void Init(int Type, int Range, int FireRate, int Strength, int Speed, float Accuracy)
	{
		type = Type;
		if (type != UNITTARGET && type != POINTTARGET)
			type = UNITTARGET;
		
		range = Range;
		firerate = FireRate;
		strength = Strength;
		speed = Speed;
		angle = 0f;
		
		// accuracy is floaing point in the bounds 0f -> 1f
		accuracy = Accuracy;
		accuracy = Math.min(accuracy, 1.0f);
		accuracy = Math.max(accuracy, 0.0f);
	}
	
	public Armament(Armament A)
	{
		Init(A.type, A.range, A.firerate, A.strength, A.speed, A.accuracy);
	}
	
	public Armament(int Type, int Range, int FireRate, int Strength, int Speed, float Accuracy)
	{
		Init(Type, Range, FireRate, Strength, Speed, Accuracy);
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
	
	public float GetAngle()
	{
		return angle;
	}
	
	public void SetAngle(float Angle)
	{
		angle = Angle;
	}
}
