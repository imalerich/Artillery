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
	 * Maximum move distance of the unit per turn.
	 */
	public final int maxmovedist;
	
	/**
	 * Requistion cost for this unit.
	 */
	public final int reqcost;
	
	/**
	 * Create a new configuration.
	 * @param Count
	 * 	default count
	 * @param Health
	 * 	max health of the unit
	 * @param Speed
	 * 	speed of the unit
	 */
	public ConfigSettings(int Count, int Health, int Speed, int MaxMoveDist, int ReqCost)
	{
		count = Count;
		health = Health;
		speed = Speed;
		maxmovedist = MaxMoveDist;
		reqcost = ReqCost;
		
		armor = new Vector<Armor>();
		arms = new Vector<Armament>();
	}
	
	/**
	 * Add a new armor configuration for the unit.
	 * @param A
	 * 	Armor to be added.
	 */
	public void addArmor(Armor A)
	{
		armor.add(A);
	}
	
	/**
	 * Add a new armament configuration for the unit.
	 * @param A
	 * 	Armament to be added.
	 */
	public void addArmament(Armament A)
	{
		arms.add(A);
	}
	
	/**
	 * Get an accessor for this configurations armor.
	 * @return
	 * Iterator of the confgiurations armors.
	 */
	public Iterator<Armor> getArmor()
	{
		return armor.iterator();
	}
	
	/**
	 * Get an accessor for this configurations armaments.
	 * @return
	 * Iterator of the configurations weapons.
	 */
	public Iterator<Armament> getArmament()
	{
		return arms.iterator();
	}
	
	/**
	 * Get the first set of armor in the list.
	 * @return
	 * 	The first set of armor.
	 */
	public Armor getFirstArmor()
	{
		return armor.firstElement();
	}
	
	/**
	 * Get the first armement in the list.
	 * @return
	 * 	The first armament.
	 */
	public Armament getFirstArmament()
	{
		return arms.firstElement();
	}
	
	/**
	 * Get the number of elements in the armor list.
	 * @return
	 * 	Number of armor elements available.
	 */
	public int armorCount()
	{
		return armor.size();
	}
	
	/**
	 * Get the number of elements in the armament list.
	 * @return
	 * 	Number of armament elements available.
	 */
	public int armamentCount()
	{
		return arms.size();
	}
}
