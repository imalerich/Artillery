package arsenal;


public class Armament 
{
	// constants that define the two possible types of armaments
	public static final int UNITTARGET = 0;
	public static final int POINTTARGET = 1;
	public static final int LANDMINE = 2;
	public static final int FLAMETARGET = 3;
	
	// generic armament properties
	private int type;
	private int range;
	private float firerate;
	private int maxfirerate;
	private int bounces;
	private int strength;
	private int speed;
	private float accuracy;
	private float angle;
	private int divcount;
	
	public final int upgrade_cost;
	public final float levelmod;
	
	public void init(int Type, int Range, float FireRate, int Strength, int Speed, float Accuracy, int MaxFireRate, int Bounces, int DivCount)
	{
		type = Type;
		range = Range;
		firerate = FireRate;
		maxfirerate = MaxFireRate;
		bounces = Bounces;
		strength = Strength;
		speed = Speed;
		angle = 0f;
		divcount = DivCount;
		
		// accuracy is a floating point in the bounds 0f -> 1f
		accuracy = Accuracy;
		accuracy = Math.min(accuracy, 1.0f);
		accuracy = Math.max(accuracy, 0.0f);
	}
	
	public Armament(Armament A)
	{
		upgrade_cost = A.upgrade_cost;
		levelmod = A.levelmod;
		
		init(A.type, A.range, A.firerate, A.strength, A.speed, A.accuracy, A.maxfirerate, A.bounces, A.divcount);
	}
	
	public Armament(int Type, int Range, int FireRate, int Strength, int Speed, float Accuracy, 
			int UpCost, float LevelMod, int MaxFireRate, int Bounces, int DivCount)
	{
		init(Type, Range, FireRate, Strength, Speed, Accuracy, MaxFireRate, Bounces, DivCount);
		
		upgrade_cost = UpCost;
		levelmod = LevelMod;
	}
	
	// getter methods for the properties of this armament
	public int getType()
	{
		return type;
	}
	
	public int getDivCount()
	{
		return divcount;
	}
	
	public void setDivCount(int C)
	{
		divcount = C;
	}
	
	public int getRange()
	{
		return range;
	}
	
	public int getBounces()
	{
		return bounces;
	}
	
	public void setBounces(int B)
	{
		bounces = B;
	}
	
	public void addRange(int R)
	{
		range = Math.min(range + R, 1024);
	}
	
	public float getFireRate()
	{
		return firerate;
	}
	
	public int getMaxFireRate()
	{
		return maxfirerate;
	}
	
	public void addFireRate(float R)
	{
		firerate = Math.min(firerate + R, maxfirerate);
	}
	
	public int getStrength()
	{
		return strength;
	}
	
	public void setStrength(int S)
	{
		strength = S;
	}
	
	public void addStrength(int S)
	{
		if (type != Armament.UNITTARGET)
			strength = Math.min(strength + S, 40);
		else
			strength = Math.min(strength + S, 20);
	}
	
	public boolean isMaxed()
	{
		if (firerate < maxfirerate)
			return false;
		
		if (type == Armament.POINTTARGET || type == Armament.LANDMINE) {
			if (strength < 40)
				return false;
			
			return true;
		} else if (type == Armament.UNITTARGET){ 
			if (strength < 20)
				return false;
			
			// range and accuracy only apply to unit target
			if (range < 1024)
				return false;

			if (accuracy < 1f)
				return false;
		}
		
		return true;
	}
	
	public float getAccuracy()
	{
		return accuracy;
	}
	
	public void addAccuracy(float A)
	{
		accuracy = Math.min(accuracy + A, 1f);
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
