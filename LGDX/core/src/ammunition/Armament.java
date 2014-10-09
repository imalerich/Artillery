package ammunition;

import com.badlogic.gdx.math.Vector2;

public class Armament 
{
	private float angle;
	private float speed;
	private Vector2 basepos;
	
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
	
	public Vector2 GetPos()
	{
		return basepos;
	}
}
