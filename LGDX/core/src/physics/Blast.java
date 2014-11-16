package physics;

import com.badlogic.gdx.math.Vector2;

public class Blast 
{
	public final Vector2 pos;
	public final float radius;
	public final float strength;
	
	public Blast(Vector2 Pos, float Radius, float Strength)
	{
		pos = Pos;
		radius = Radius;
		strength = Strength;
	}
}
