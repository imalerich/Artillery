package particles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;

public class Particle 
{
	private static final int GRAVITY = 80;
	private static final float DECAY = 6f;
	
	private float radius;
	private Vector2 pos;
	private Vector2 vel;
	
	public Particle(float Radius, Vector2 Pos, Vector2 Vel)
	{
		radius = Radius;
		pos = Pos;
		vel = Vel;
	}
	
	public float GetSize()
	{
		return radius;
	}
	
	public Vector2 GetPos()
	{
		return pos;
	}
	
	public Vector2 GetVelocity()
	{
		return vel;
	}
	
	public boolean IsAlive()
	{
		return radius > 8f;
	}
	
	public void Update()
	{
		pos.x += vel.x * Gdx.graphics.getDeltaTime();
		pos.y += vel.y * Gdx.graphics.getDeltaTime();
		pos.y += GRAVITY * Gdx.graphics.getDeltaTime();
		
		radius -= Gdx.graphics.getDeltaTime() * DECAY;
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam)
	{
		ParticleMask.AddVisibleRegion(Cam.GetRenderX(pos.x), Cam.GetRenderY(pos.y), (int)radius);
	}
}
