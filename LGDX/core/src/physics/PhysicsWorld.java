package physics;

import java.util.Iterator;
import java.util.Vector;

import terrain.Background;
import terrain.FogOfWar;
import terrain.Terrain;
import ammunition.Projectile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Camera;

import entity.Army;

public class PhysicsWorld 
{
	private Terrain ter;
	private Vector<Army> friendlyArmy;
	private Vector<Army> enemyArmy;
	private Vector<Projectile> projectiles;
	
	public PhysicsWorld(Terrain Ter)
	{
		ter = Ter;
		
		friendlyArmy = new Vector<Army>();
		enemyArmy = new Vector<Army>();
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
	
	public void AddFriendlyArmy(Army Add)
	{
		friendlyArmy.add(Add);
	}
	
	public void AddEnemyArmy(Army Add)
	{
		enemyArmy.add(Add);
	}
	
	public void Update(Camera Cam)
	{
		ter.Update();
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().Update(Cam);
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().Update(Cam);
			
		Iterator<Projectile> p = projectiles.iterator();
		while (p.hasNext())
			p.next().Update();
	}
	
	private void DrawFogMask(SpriteBatch Batch, Camera Cam)
	{
		FogOfWar.Begin(Batch);
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().DrawView(Cam);
		FogOfWar.End(Batch);
	}
	
	private void DrawHidden(SpriteBatch Batch, Camera Cam)
	{
		// enable fog of war and draw the background
		FogOfWar.MaskOn(Batch);
		Background.DrawFG(Batch);
		Batch.flush();
		
		// temporarily disable the stencil test to draw the terrain
		Gdx.gl.glDisable(GL20.GL_STENCIL_TEST);
		ter.Draw(Batch, Cam.GetPos());
		Batch.flush();
		Gdx.gl.glEnable(GL20.GL_STENCIL_TEST);
		
		// draw all enemy untis above the terrain, but hidden by the fog 
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().DrawBase(Batch, Cam);
		
		e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().Draw(Batch, Cam);
		
		FogOfWar.MaskOff(Batch);
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam)
	{
		Background.DrawBG(Batch);
		DrawFogMask(Batch, Cam);
		DrawHidden(Batch, Cam);
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().DrawBase(Batch, Cam);
		
		f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().Draw(Batch, Cam);
		
		Iterator<Projectile> p = projectiles.iterator();
		while (p.hasNext())
			p.next().Draw(Batch, Cam);
	}
}