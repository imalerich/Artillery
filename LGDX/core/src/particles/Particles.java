package particles;

import java.util.Arrays;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;

public class Particles 
{
	private static final int SIZE = 1000;
	private static final float DEFAULTDECAY = 5f;
	
	private Ember[] embers;
	private Particle[] particles;
	private int addindex;
	private int addember;
	
	public Particles()
	{
		particles = new Particle[SIZE];
		embers = new Ember[SIZE];
		addindex = 0;
		addember = 0;
	}
	
	public void addEmber(float Scale, Vector2 Pos)
	{
		addEmber(Scale, Pos, new Vector2(), 0f, 0.5f);
	}
	
	public void addEmber(float Scale, Vector2 Pos, Vector2 Vel, float SlowTime, float Decay)
	{
		if (Scale <= 0f)
			return;
		
		float xpos = Pos.x - Ember.tex.getWidth()/2f;
		float ypos = Pos.y - Ember.tex.getHeight()/2f;
		
		expandEmbers();
		embers[addember] = new Ember(Scale, new Vector2(xpos, ypos), Vel, SlowTime, Decay);
		addember++;
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
		
		for (int i=0; i<addember; i++)
		{
			embers[i].update();
			if (!embers[i].isAlive())
				removeEmber(i);
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
		
		Batch.setColor(1f, 1f, 1f, 0.5f);
		for (int i=0; i<addember; i++)
		{
			embers[i].draw(Batch,  Cam);
			if (!embers[i].isAlive())
				removeEmber(i);
		}
		
		Batch.setColor(1f);
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
	
	private void expandEmbers()
	{
		if (addember >= embers.length) {
			// increase the size of the array
			embers = Arrays.copyOf(embers, embers.length+SIZE);
		}
	}
	
	private void remove(int Index)
	{
		// replace this particle with the last particle added
		particles[Index] = particles[addindex-1];
		addindex--;
	}
	
	private void removeEmber(int Index)
	{
		// replace this particle with the last particle added
		embers[Index] = embers[addember-1];
		addember--;
	}
}
