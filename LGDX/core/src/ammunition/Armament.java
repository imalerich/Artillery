package ammunition;

import physics.PhysicsWorld;

import com.badlogic.gdx.math.Vector2;

public class Armament 
{
	private float angle;
	private float speed;
	
	public void Fire(PhysicsWorld World, Vector2 BasePos)
	{
		// override in implementation class
	}
	
	public void AddAngle(float Add)
	{
		angle += Add;
	}
	
	public float GetAngle()
	{
		return angle;
	}
	
	public void SetSpeed(float Speed)
	{
		speed = Speed;
	}
	
	public float GetSpeed()
	{
		return speed;
	}
	
	public Vector2 GetInitialVelocity()
	{
		return new Vector2( (float)Math.cos(angle)*speed, (float)Math.sin(angle)*speed );
	}
}
