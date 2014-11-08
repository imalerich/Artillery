package arsenal;

public class Armor 
{
	private final int maxhealth;
	private final int maxstrength;
	
	private int health;
	private int strength;
	
	public Armor(int Health, int Strength)
	{
		maxhealth = Health;
		maxstrength = Strength;
		
		health = Health;
		strength = Strength;
	}
	
	public void Damage(int Ammount)
	{
		health = Math.max(health-Ammount, 0);
		
		// the less health the armor has, the less damage it can withstand
		strength = (int)(maxstrength * ((float)health/maxhealth));
	}
	
	public int GetHealth()
	{
		return health;
	}
	
	public int GetStrength()
	{
		return strength;
	}
}
