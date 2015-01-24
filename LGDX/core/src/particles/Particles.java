package particles;

import java.util.Arrays;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;

public class Particles 
{
	private static final int SIZE = 100;
	private static final float DEFAULTDECAY = 5f;
	private Particle[] particles;
	private int addindex;
	
	public Particles()
	{
		particles = new Particle[SIZE];
		addindex =  0;
	}
	
	public void addParticle(float Radius, Vector2 Pos)
	{
		if (Radius <= 0f)
			return;
		
		expand();
		particles[addindex] = new Particle(Radius, Pos, new Vector2(), 0f, DEFAULTDECAY);
		addindex++;
	}
	
	public void addParticle(float Radius, Vector2 Pos, Vector2 Vel)
	{
		if (Radius <= 0f)
			return;
		
		expand();
		particles[addindex] = new Particle(Radius, Pos, Vel, 0f, DEFAULTDECAY);
		addindex++;
	}
	
	public void addParticle(float Radius, Vector2 Pos, Vector2 Vel, float SlowTime, float Decay)
	{
		if (Radius <= 0f)
			return;
		
		expand();
		particles[addindex] = new Particle(Radius, Pos, Vel, SlowTime, Decay);
		addindex++;
	}
	
	public void update()
	{
		for (int i=0; i<addindex; i++)
		{
			particles[i].update();
			if (!particles[i].isAlive())
				remove(i);
		}
	}
	
	public void draw(SpriteBatch Batch, Camera Cam)
	{
		ParticleMask.begin(Batch);
		for (int i=0; i<addindex; i++)
		{
			particles[i].draw(Batch, Cam);
			if (!particles[i].isAlive())
				remove(i);
		}
		
		ParticleMask.end(Batch);
		ParticleMask.draw(Batch);
		Batch.flush();
	}
	
	public void drawParallax(SpriteBatch Batch, Camera Cam, float XPos)
	{
		ParticleMask.begin(Batch);
		for (int i=0; i<addindex; i++)
		{
			particles[i].drawParallax(Batch, Cam, XPos);
			if (!particles[i].isAlive())
				remove(i);
		}
		
		ParticleMask.end(Batch);
		ParticleMask.draw(Batch);
		Batch.flush();
	}
	
	private void expand()
	{
		if (addindex >= particles.length) {
			// increase the size of the array
			particles = Arrays.copyOf(particles, particles.length+SIZE);
		}
	}
	
	private void remove(int Index)
	{
		// replace this particle with the last particle added
		particles[Index] = particles[addindex-1];
		addindex--;
	}
}
