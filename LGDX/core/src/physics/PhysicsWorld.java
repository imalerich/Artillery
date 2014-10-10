package physics;

import java.util.Iterator;
import java.util.Vector;

import terrain.Terrain;
import ammunition.Projectile;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Camera;
import com.mygdx.game.MilitaryBase;

import entity.Squad;

public class PhysicsWorld 
{
	private Terrain ter;
	private Vector<MilitaryBase> bases;
	private Vector<Squad> squads;
	private Vector<Projectile> projectiles;
	
	public PhysicsWorld(Terrain Ter)
	{
		ter = Ter;
		
		bases = new Vector<MilitaryBase>();
		squads = new Vector<Squad>();
		projectiles = new Vector<Projectile>();
	}
	
	public void Release()
	{
		ter.Release();
	}
	
	public Terrain GetTerrain()
	{
		return ter;
	}
	
	public void AddSquad(Squad Add)
	{
		squads.add(Add);
	}
	
	public void AddProjectile(Projectile Add)
	{
		projectiles.add(Add);
	}
	
	public void AddBase(MilitaryBase Add)
	{
		bases.add(Add);
	}
	
	public void Update(Camera Cam)
	{
		ter.Update();
		
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext())
			s.next().Update(Cam.GetPos());
			
		Iterator<Projectile> p = projectiles.iterator();
		while (p.hasNext())
			p.next().Update();
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam)
	{
		ter.Draw(Batch, Cam.GetPos());
		
		Iterator<MilitaryBase> b = bases.iterator();
		while (b.hasNext())
			b.next().Draw(Batch, Cam);
		
		Iterator<Squad> s = squads.iterator();
		while (s.hasNext())
			s.next().Draw(Batch, Cam);
		
		Iterator<Projectile> p = projectiles.iterator();
		while (p.hasNext())
			p.next().Draw(Batch, Cam);
	}
}