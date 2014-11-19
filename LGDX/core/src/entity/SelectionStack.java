package entity;

import java.util.Iterator;
import java.util.Vector;

public class SelectionStack
{
	public static final int OVERSQUAD = 0;
	public static final int OVERADD = 1;
	
	private Vector<SelectionElement> references;
	private int selection;
	
	public SelectionStack()
	{
		selection = 0;
		references = new Vector<SelectionElement>();
	}
	
	public void Reset()
	{
		selection = 0;
		references.clear();
	}
	
	public int GetSize()
	{
		return references.size();
	}
	
	public void AddSquadOver(Squad Ref)
	{
		references.add( new SelectionElement(OVERSQUAD, Ref) );
	}
	
	public void AddBarracksOver()
	{
		references.add( new SelectionElement(OVERADD, null) );
	}
	
	public void IncSelection()
	{
		selection++;
		
		// wrap to 0
		if (selection == references.size())
			selection = 0;
	}
	
	public void DecSelection()
	{
		selection--;
		
		// wrap to max
		if (selection < 0)
			selection = references.size()-1;
	}
	
	public boolean IsSelectionValid()
	{
		if (references.size() == 0)
			return false;
		else if (selection >= references.size())
			return false;
		else if (selection < 0)
			return false;
		
		return true;
	}
	
	public boolean IsOverSquad()
	{
		if (!IsSelectionValid())
			return false;
		
		return (references.get(selection).action == OVERSQUAD);
	}
	
	public boolean IsOverAdd()
	{
		if (!IsSelectionValid())
			return false;
		
		return (references.get(selection).action == OVERADD);
	}
	
	public Squad GetSquadOver()
	{
		if (!IsSelectionValid())
			return null;
		
		return references.get(selection).ref;
	}
	
	public Iterator<SelectionElement> GetIterator()
	{
		return references.iterator();
	}
}
