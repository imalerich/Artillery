package physics;

import java.util.Iterator;
import java.util.Vector;

import terrain.Terrain;
import ammunition.Projectile;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Camera;

import entity.Army;

public class PhysicsWorld 
{
	private Terrain ter;
	private Vector<Army> armies;
	private Vector<Projectile> projectiles;
	
	public PhysicsWorld(Terrain Ter)
	{
		ter = Ter;
		
		armies = new Vector<Army>();
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
	
	public void AddProjectile(Projectile Add)
	{
		projectiles.add(Add);
	}
	
	public void AddArmy(Army Add)
	{
		armies.add(Add);
	}
	
	public void Update(Camera Cam)
	{
		ter.Update();
		
		Iterator<Army> a = armies.iterator();
		while (a.hasNext())
			a.next().Update(Cam);
			
		Iterator<Projectile> p = projectiles.iterator();
		while (p.hasNext())
			p.next().Update();
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam)
	{
		ter.Draw(Batch, Cam.GetPos());
		
		Iterator<Army> a = armies.iterator();
		while (a.hasNext())
			a.next().DrawBase(Batch, Cam);
		
		a = armies.iterator();
		while (a.hasNext())
			a.next().Draw(Batch, Cam);
		
		Iterator<Projectile> p = projectiles.iterator();
		while (p.hasNext())
			p.next().Draw(Batch, Cam);
	}
}