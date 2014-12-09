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
	
	public void init(int Type, int Range, int FireRate, int Strength, int Speed, float Accuracy)
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
		init(A.type, A.range, A.firerate, A.strength, A.speed, A.accuracy);
	}
	
	public Armament(int Type, int Range, int FireRate, int Strength, int Speed, float Accuracy)
	{
		init(Type, Range, FireRate, Strength, Speed, Accuracy);
	}
	
	// getter methods for the properties of this armament
	public int getType()
	{
		return type;
	}
	
	public int getRange()
	{
		return range;
	}
	
	public int getFireRate()
	{
		return firerate;
	}
	
	public int getStrength()
	{
		return strength;
	}
	
	public float getAccuracy()
	{
		return accuracy;
	}
	
	public int getSpeed()
	{
		return speed;
	}
	
	public float getAngle()
	{
		return angle;
	}
	
	public void setAngle(float Angle)
	{
		angle = Angle;
	}
}
