package entity;

import java.util.Vector;

public class SelectionStack
{
	public static final int OVERSQUAD = 0;
	public static final int OVERADD = 1;
	
	private Vector<Integer> actions;
	private Vector<Squad> reference;
	private int selection;
	
	public SelectionStack()
	{
		selection = 0;
		actions = new Vector<Integer>();
		reference = new Vector<Squad>();
	}
	
	public void Reset()
	{
		selection = 0;
		actions.clear();
		reference.clear();
	}
	
	public int GetSize()
	{
		return actions.size();
	}
	
	public void AddSquadOver(Squad Ref)
	{
		actions.add(OVERSQUAD);
		reference.add(Ref);
	}
	
	public void AddBarracksOver()
	{
		actions.add(OVERADD);
		reference.add(null);
	}
	
	public void IncSelection()
	{
		selection++;
		
		// wrap to 0
		if (selection == actions.size())
			selection = 0;
	}
	
	public void DecSelection()
	{
		selection--;
		
		// wrap to max
		if (selection < 0)
			selection = actions.size()-1;
	}
	
	public boolean IsSelectionValid()
	{
		if (actions.size() == 0)
			return false;
		else if (selection >= reference.size())
			return false;
		else if (selection < 0)
			return false;
		
		return true;
	}
	
	public boolean IsOverSquad()
	{
		if (!IsSelectionValid())
			return false;
		
		return (actions.get(selection) == OVERSQUAD);
	}
	
	public boolean IsOverAdd()
	{
		if (!IsSelectionValid())
			return false;
		
		return (actions.get(selection) == OVERADD);
	}
	
	public Squad GetSquadOver()
	{
		if (!IsSelectionValid())
			return null;
		
		return reference.get(selection);
	}
}
