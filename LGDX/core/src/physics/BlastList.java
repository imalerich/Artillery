package physics;

import java.util.Iterator;
import java.util.Vector;

import com.badlogic.gdx.math.Vector2;

public class BlastList 
{
	private Vector<Blast> blasts;
	
	public BlastList()
	{
		blasts = new Vector<Blast>();
	}
	
	public void AddBlast(Vector2 Pos, float Radius, float Strength)
	{
		blasts.add( new Blast(Pos, Radius, Strength) );
	}
	
	public Iterator<Blast> GetBlastIterator()
	{
		return blasts.iterator();
	}
}
