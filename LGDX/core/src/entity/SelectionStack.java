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
	
	public void reset()
	{
		selection = 0;
		references.clear();
	}
	
	public int getSize()
	{
		return references.size();
	}
	
	public void addSquadOver(Squad Ref)
	{
		references.add( 0, new SelectionElement(OVERSQUAD, Ref) );
	}
	
	public void addBarracksOver()
	{
		references.add( 0, new SelectionElement(OVERADD, null) );
	}
	
	public void incSelection()
	{
		selection++;
		
		// wrap to 0
		if (selection == references.size())
			selection = 0;
	}
	
	public void decSelection()
	{
		selection--;
		
		// wrap to max
		if (selection < 0)
			selection = references.size()-1;
	}
	
	public boolean isSelectionValid()
	{
		if (references.size() == 0)
			return false;
		else if (selection >= references.size())
			return false;
		else if (selection < 0)
			return false;
		
		return true;
	}
	
	public boolean isOverSquad()
	{
		if (!isSelectionValid())
			return false;
		
		return (references.get(selection).action == OVERSQUAD);
	}
	
	public boolean isOverAdd()
	{
		if (!isSelectionValid())
			return false;
		
		return (references.get(selection).action == OVERADD);
	}
	
	public Squad getSquadOver()
	{
		if (!isSelectionValid())
			return null;
		
		return references.get(selection).ref;
	}
	
	public Iterator<SelectionElement> getIterator()
	{
		return references.iterator();
	}
}
