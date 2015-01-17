package config;

import java.util.Iterator;
import java.util.Vector;

import arsenal.Armament;
import arsenal.Armor;

public class ConfigSettings 
{
	// store a list of available armor and weapons
	private Vector<Armor> armor;
	private Vector<Armament> primary;
	private Vector<Armament> secondary;
	
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
	 * Requisition cost for this unit.
	 */
	public final int reqcost;
	
	/**
	 * Requisition bonus for this unit.
	 */
	public final int reqbonus;
	
	/**
	 * Create a new configuration.
	 * @param Count
	 * 	default count
	 * @param Health
	 * 	max health of the unit
	 * @param Speed
	 * 	speed of the unit
	 */
	public ConfigSettings(int Count, int Health, int Speed, int MaxMoveDist, int ReqCost, int ReqBonus)
	{
		count = Count;
		health = Health;
		speed = Speed;
		maxmovedist = MaxMoveDist;
		reqcost = ReqCost;
		reqbonus = ReqBonus;
		
		armor = new Vector<Armor>();
		primary = new Vector<Armament>();
		secondary = new Vector<Armament>();
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
	 * Add a new primary armament configuration for the unit.
	 * @param A
	 * 	Primary armament to be added.
	 */
	public void addPrimary(Armament A)
	{
		primary.add(A);
	}
	
	/**
	 * Add a new secondary armament configuration for the unit.
	 * @param A
	 * 	Secondary armament to be added.
	 */
	public void addSecondary(Armament A)
	{
		secondary.add(A);
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
	 * Get an accessor for this configurations primary armaments.
	 * @return
	 * Iterator of the configurations primary weapons.
	 */
	public Iterator<Armament> getPrimary()
	{
		return primary.iterator();
	}
	
	/**
	 * Get an accessor for this configurations armaments.
	 * @return
	 * Iterator of the configurations secondary weapons.
	 */
	public Iterator<Armament> getSeconary()
	{
		return secondary.iterator();
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
	 * Get the first primary armement in the list.
	 * @return
	 * 	The first primary armament.
	 */
	public Armament getFirstPrimary()
	{
		return primary.firstElement();
	}
	
	/**
	 * Get the first armement in the list.
	 * @return
	 * 	The first armament.
	 */
	public Armament getFirstSecondary()
	{
		return secondary.firstElement();
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
	 * Get the number of elements in the primary armament list.
	 * @return
	 * 	Number of primary armament elements available.
	 */
	public int primaryCount()
	{
		return primary.size();
	}
	
	/**
	 * Get the number of elements in the secondary armament list.
	 * @return
	 * 	Number of secondary armament elements available.
	 */
	public int secondaryCount()
	{
		return secondary.size();
	}
}
