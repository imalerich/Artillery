package arsenal;

public class Armor 
{
	private int health;
	private int strength;
	
	public Armor(int Health, int Strength)
	{
		health = Health;
		strength = Strength;
	}
	
	public void Damage(int Ammount)
	{
		health = Math.max(health-Ammount, 0);
	}
	
	public int GetHeight()
	{
		return health;
	}
	
	public int GetStrength()
	{
		return strength;
	}
}
