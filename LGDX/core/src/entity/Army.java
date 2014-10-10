package entity;

import java.util.Iterator;
import java.util.Vector;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Camera;
import com.mygdx.game.MilitaryBase;

public class Army 
{
	MilitaryBase base;
	Vector<Squad> squads;
	
	public Army(MilitaryBase Base)
	{
		base = Base;
		squads = new Vector<Squad>();
	}
	
	public void AddSquad(Squad Add)
	{
		squads.add(Add);
	}
	
	public void Update(Camera Cam)
	{
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext())
			s.next().Update(Cam.GetPos());
	}
	
	public void DrawBase(SpriteBatch Batch, Camera Cam)
	{
		base.Draw(Batch, Cam);
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam)
	{
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext())
			s.next().Draw(Batch, Cam);
	}
}
