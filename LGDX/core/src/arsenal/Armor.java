package arsenal;

public class Armor 
{
	private final int maxhealth;
	private final int maxstrength;
	
	private int health;
	private int strength;
	
	public Armor(Armor A)
	{
		maxhealth = A.maxhealth;
		maxstrength = A.maxstrength;
		
		health = maxhealth;
		strength = maxstrength;
	}
	
	public Armor(int Health, int Strength)
	{
		maxhealth = Health;
		maxstrength = Strength;
		
		health = Health;
		strength = Strength;
	}
	
	public void damage(int Ammount)
	{
		health = Math.max(health-Ammount, 0);
		
		// the less health the armor has, the less damage it can withstand
		strength = (int)(maxstrength * ((float)health/maxhealth));
	}
	
	public int getHealth()
	{
		return health;
	}
	
	public int getStrength()
	{
		return strength;
	}
}
