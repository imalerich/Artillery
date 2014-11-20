package config;

import java.util.Iterator;
import java.util.Vector;

import arsenal.Armament;
import arsenal.Armor;

public class ConfigSettings 
{
	// store a list of available armor and weapons
	private Vector<Armor> armor;
	private Vector<Armament> arms;
	
	/**
	 * Default count of a unit.
	 */
	public final int count;
	
	/**
	 * Max health of a unit.
	 */
	public final int health;
	
	/**
	 * Movement speed of a unit.
	 */
	public final int speed;
	
	/**
	 * Create a new configuration.
	 * @param Count
	 * 	default count
	 * @param Health
	 * 	max health of the unit
	 * @param Speed
	 * 	speed of the unit
	 */
	public ConfigSettings(int Count, int Health, int Speed)
	{
		count = Count;
		health = Health;
		speed = Speed;
		
		armor = new Vector<Armor>();
		arms = new Vector<Armament>();
	}
	
	/**
	 * Add a new armor configuration for the unit.
	 * @param A
	 * 	Armor to be added.
	 */
	public void AddArmor(Armor A)
	{
		armor.add(A);
	}
	
	/**
	 * Add a new armament configuration for the unit.
	 * @param A
	 * 	Armament to be added.
	 */
	public void AddArmament(Armament A)
	{
		arms.add(A);
	}
	
	/**
	 * Get an accessor for this configurations armor.
	 * @return
	 * Iterator of the confgiurations armors.
	 */
	public Iterator<Armor> GetArmor()
	{
		return armor.iterator();
	}
	
	/**
	 * Get an accessor for this configurations armaments.
	 * @return
	 * Iterator of the configurations weapons.
	 */
	public Iterator<Armament> GetArmament()
	{
		return arms.iterator();
	}
	
	/**
	 * Get the number of elements in the armor list.
	 * @return
	 * 	Number of armor elements available.
	 */
	public int ArmorCount()
	{
		return armor.size();
	}
	
	/**
	 * Get the number of elements in the armament list.
	 * @return
	 * 	Number of armament elements available.
	 */
	public int ArmamentCount()
	{
		return arms.size();
	}
}
