package particles;

import java.util.Arrays;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;

public class Particles 
{
	private static final int SIZE = 100;
	private Particle[] particles;
	private int addindex;
	
	public Particles()
	{
		particles = new Particle[SIZE];
		addindex =  0;
	}
	
	public void AddParticle(float Radius, Vector2 Pos)
	{
		if (Radius <= 0f)
			return;
		
		Expand();
		particles[addindex] = new Particle(Radius, Pos, new Vector2(), 0f);
		addindex++;
	}
	
	public void AddParticle(float Radius, Vector2 Pos, Vector2 Vel)
	{
		if (Radius <= 0f)
			return;
		
		Expand();
		particles[addindex] = new Particle(Radius, Pos, Vel, 0f);
		addindex++;
	}
	
	public void AddParticle(float Radius, Vector2 Pos, Vector2 Vel, float SlowTime)
	{
		if (Radius <= 0f)
			return;
		
		Expand();
		particles[addindex] = new Particle(Radius, Pos, Vel, SlowTime);
		addindex++;
	}
	
	public void Update()
	{
		for (int i=0; i<addindex; i++)
		{
			particles[i].Update();
			if (!particles[i].IsAlive())
				Remove(i);
		}
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam)
	{
		ParticleMask.Begin(Batch);
		for (int i=0; i<addindex; i++)
		{
			particles[i].Draw(Batch, Cam);
			if (!particles[i].IsAlive())
				Remove(i);
		}
		
		ParticleMask.End(Batch);
		ParticleMask.Draw(Batch);
	}
	
	private void Expand()
	{
		if (addindex >= particles.length) {
			// increase the size of the array
			particles = Arrays.copyOf(particles, particles.length+SIZE);
		}
	}
	
	private void Remove(int Index)
	{
		// replace this particle with the last particle added
		particles[Index] = particles[addindex-1];
		addindex--;
	}
}
