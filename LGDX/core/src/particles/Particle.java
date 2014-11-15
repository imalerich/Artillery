package particles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;

public class Particle 
{
	public static final int GRAVITY = 40;
	private static final float DECAY = 5f;
	
	private float radius;
	private Vector2 pos;
	private Vector2 vel;
	private Vector2 slowrate;
	
	public Particle(float Radius, Vector2 Pos, Vector2 Vel, float SlowTime)
	{
		radius = Radius;
		pos = Pos;
		vel = Vel;
		slowrate = new Vector2();
		if (SlowTime != 0f)	 {
			slowrate = new Vector2(vel.x/SlowTime, vel.y/SlowTime);
			slowrate.x = Math.abs(vel.x);
			slowrate.y = Math.abs(vel.y);
		}
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
		return radius > 0f;
	}
	
	public void Update()
	{
		ModVel();
		pos.x += vel.x * Gdx.graphics.getDeltaTime();
		pos.y += vel.y * Gdx.graphics.getDeltaTime();
		pos.y += GRAVITY * Gdx.graphics.getDeltaTime();
		
		if (pos.x >= Game.WORLDW)
			pos.x -= Game.WORLDW;
		else if (pos.x < 0)
			pos.x += Game.WORLDW;
		
		radius -= Gdx.graphics.getDeltaTime() * DECAY;
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam)
	{
		ParticleMask.AddVisibleRegion(Cam.GetRenderX(pos.x), Cam.GetRenderY(pos.y), (int)radius);
	}
	
	private void ModVel()
	{
		float time = Gdx.graphics.getDeltaTime();
		if (vel.x > 0) {
			vel.x -= slowrate.x * time;
			if (vel.x < 0) {
				vel.x = 0f;
			}
			
		} else if (vel.x < 0) {
			vel.x += slowrate.x * time;
			if (vel.x > 0) {
				vel.x = 0f;
			}
		}
		
		if (vel.y > 0) {
			vel.y -= slowrate.y * time;
			if (vel.y < 0) {
				vel.y = 0f;
			}
			
		} else if (vel.y < 0) {
			vel.y += slowrate.y * time;
			if (vel.y > 0) {
				vel.y = 0f;
			}
		}
	}
}
