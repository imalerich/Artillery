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
		System.out.println("Dealing " + Ammount + " in damage to Units armor.");
		health = Math.max(health-Ammount, 0);
		System.out.println("Armor health is now at " + health + '.');
		
		// the less health the armor has, the less damage it can withstand
		strength = (int)(maxstrength * ((float)health/maxhealth));
		System.out.println("Armor strength is now at " + strength + ".\n");
	}
	
	public int getMaxHealth()
	{
		return maxhealth;
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
