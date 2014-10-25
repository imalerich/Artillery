package physics;

import arsenal.Armament;
import entity.Unit;

public class CombatPacket 
{
	private final Unit offense;
	private final Unit defense;
	private final Armament arms;
	private boolean iscompleted;
	
	public CombatPacket(Unit Offense, Unit Defense, Armament Arms)
	{
		offense = Offense;
		defense = Defense;
		arms = Arms;
		iscompleted = false;
	}
	
	public Unit GetOffense()
	{
		return offense;
	}
	
	public Unit GetDefense()
	{
		return defense;
	}
	
	public Armament GetArmament()
	{
		return arms;
	}
	
	public void SetCompleted()
	{
		iscompleted = true;
	}
	
	public boolean IsCompleted()
	{
		return iscompleted;
	}
}
