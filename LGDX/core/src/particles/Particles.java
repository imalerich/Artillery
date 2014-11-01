package particles;

import java.util.Iterator;
import java.util.Vector;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;

public class Particles 
{
	private Vector<Particle> particles;
	
	public Particles()
	{
		particles = new Vector<Particle>();
	}
	
	public void AddParticle(float Radius, Vector2 Pos)
	{
		if (Radius <= 0f)
			return;
		
		particles.add(new Particle(Radius, Pos, new Vector2()));
	}
	
	public void AddParticle(float Radius, Vector2 Pos, Vector2 Vel)
	{
		if (Radius <= 0f)
			return;
		
		particles.add(new Particle(Radius, Pos, Vel));
	}
	
	public void Update()
	{
		Iterator<Particle> p = particles.iterator();
		while (p.hasNext()) {
			Particle next = p.next();
			
			next.Update();
			if (!next.IsAlive())
				p.remove();
		}
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam)
	{
		ParticleMask.Begin(Batch);
		Iterator<Particle> p = particles.iterator();
		while (p.hasNext()) {
			p.next().Draw(Batch, Cam);;
		}
		
		ParticleMask.End(Batch);
		ParticleMask.Draw(Batch);
	}
}
