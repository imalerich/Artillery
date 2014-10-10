package ammunition;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;

public class Projectile 
{
	protected Vector2 pos;
	protected Vector2 vel;
	protected boolean ignoreGravity;
	
	protected float radius;
	protected float blastradius;
	
	public float GetRadius()
	{
		return radius;
	}
	
	public float GetBlastRadius()
	{
		return blastradius;
	}
	
	public Vector2 GetPos()
	{
		return pos;
	}
	
	public Vector2 GetVelocity()
	{
		return vel;
	}
	
	public void Update()
	{
		if (!ignoreGravity)
			vel.y -= Gdx.graphics.getDeltaTime()*Game.GRAVITY;
		
		pos.x += vel.x*Gdx.graphics.getDeltaTime();
		pos.y += vel.y*Gdx.graphics.getDeltaTime();
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam)
	{
		// override in implementation classes
	}
}
