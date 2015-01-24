package physics;

import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;

public class LandMine 
{
	private static Texture tex;
	private final Terrain ter;
	private final GameWorld world;
	private final int army;
	private final int strength;
	
	private Rectangle bbox;
	private Vector2 pos;
	private float initialY;
	private float delay = 0.1f;
	
	private float timer = 0f;
	private boolean detonate = false;
	
	public static void init()
	{
		if (tex == null)
			tex = new Texture( Gdx.files.internal("img/weaponry/landmine.png") );
	}
	
	public static void release()
	{
		if (tex != null)
			tex.dispose();
	}
	
	public LandMine(float XPos, Terrain Ter, GameWorld World, int Army, int Strength)
	{
		pos = new Vector2(XPos, Game.WORLDH - Ter.getHeight( (int)XPos ));
		pos.x -= tex.getWidth()/2f;
		initialY = pos.y;
		
		ter = Ter;
		army = Army;
		world = World;
		strength = Strength;
		
		bbox = new Rectangle(pos.x, pos.y - tex.getWidth()/2f, tex.getWidth(), tex.getWidth());
	}
	
	public boolean update()
	{
		if (detonate) {
			timer += Gdx.graphics.getDeltaTime();
			
			if (timer > delay) {
				detonate();
				return true;
			}
		}
		
		return false;
	}
	
	public void checkUnitMove(Rectangle BBox, int Army)
	{
		if (Army == army)
			return;
		
		if (BBox.contains(bbox) || BBox.overlaps(bbox)) {
			detonate = true;
			delay = 0.4f;
		}
	}
	
	public void procBlast(Blast B)
	{
		if (Intersector.overlaps(B.getBoundingCircle(), bbox) ||
				bbox.contains(B.pos)  && !detonate) {
			detonate = true;
		}
	}
	
	public void detonate()
	{
		world.procBlast( new Blast(new Vector2(pos.x + tex.getWidth()/2f, pos.y + tex.getHeight()/2f), 32, strength, army) );
	}
	
	public void draw(SpriteBatch Batch, Camera Cam, int UserArmy)
	{
		if (army != UserArmy)
			return;
		
		float y0 = Game.WORLDH - ter.getHeight( (int)pos.x );
		float y1 = Game.WORLDH - ter.getHeight( (int)(pos.x + tex.getWidth()) );
		float theta = (float)Math.atan( (y1-y0)/(tex.getWidth()) );
		theta = (float)Math.toDegrees(theta);
		
		pos.y = Game.WORLDH - ter.getHeight( (int)(pos.x + tex.getWidth()/2) ) - 1;
		
		// detonate after falling
		if (Math.abs(pos.y - initialY) > 4f)
			detonate = true;
		
		bbox = new Rectangle(pos.x, pos.y - tex.getWidth()/2f, tex.getWidth(), tex.getWidth());
		
		float xpos = Cam.getRenderX(pos.x);
		float ypos = Cam.getRenderY(pos.y);
		Batch.draw(tex, xpos, ypos, tex.getWidth()/2f, 0, tex.getWidth(), tex.getHeight(), 1f, 1f, theta, 
				0, 0, tex.getWidth(), tex.getHeight(), false, false);
	}
}
