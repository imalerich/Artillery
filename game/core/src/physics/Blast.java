package physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;

public class Blast 
{
	public final Vector2 pos;
	public final float radius;
	public final float strength;
	
	private static ShapeRenderer sr;
	private double time = 0f;
	
	private final int sourceArmy;
	
	public static void init()
	{
		sr = new ShapeRenderer();
	}
	
	public static void release()
	{
		if (sr != null)
			sr.dispose();
	}
	
	public Blast(Vector2 Pos, float Radius, float Strength, int SourceArmy)
	{
		pos = new Vector2(Pos);
		radius = Radius;
		strength = Strength;
		sourceArmy = SourceArmy;
	}
	
	public int getSourceArmy()
	{
		return sourceArmy;
	}
	
	public Circle getBoundingCircle()
	{
		return new Circle(pos, radius);
	}
	
	public void update()
	{
		//
	}
	
	public boolean isAlive()
	{
		return time < 0.5f;
	}
	
	public static void begin(SpriteBatch Batch)
	{
		Batch.end();
		sr.setProjectionMatrix(Game.getProj().combined);
		sr.begin(ShapeType.Filled);
	}
	
	public static void end(SpriteBatch Batch)
	{
		sr.end();
		Batch.begin();
	}
	
	public void draw(Camera Cam)
	{
		time += Gdx.graphics.getDeltaTime();
		float c = (float)(Math.pow(time/0.5f, 2f));
		
		sr.setColor(c, c, c, 1f);
		sr.circle(Cam.getRenderX(pos.x), Cam.getRenderY(pos.y), radius*1.1f);
		sr.setColor(1f, 1f, 1f, 1f);
	}
}
