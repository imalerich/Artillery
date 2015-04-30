package objects;

import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;

public class TankBarrier 
{
	public static final float MOVESPEED = 64;
	public static final int MOUNDWIDTH = 29;
	public static Texture TANKBARRIER;
	
	private final Terrain ter;
	private final Vector2 pos;
	private Rectangle bbox;
	private double time;
	
	public static void init()
	{
		if (TANKBARRIER == null)
			TANKBARRIER = new Texture( Gdx.files.internal("img/objects/barricade.png") );
	}
	
	public static void release()
	{
		if (TANKBARRIER != null)
			TANKBARRIER.dispose();
	}
	
	public TankBarrier(Vector2 Pos, Terrain Ter)
	{
		time = 0.0;
		ter = Ter;
		pos = new Vector2(Pos);
		
		// set the mound lower and have it rise out of the ground
		pos.y -= TANKBARRIER.getHeight();
		bbox = new Rectangle(pos.x, 0, TANKBARRIER.getWidth(), Game.WORLDH);
	}
	
	public Vector2 getPos()
	{
		return pos;
	}
	
	public Rectangle getBBox()
	{
		return bbox;
	}
	
	public void update()
	{
		if (time < TANKBARRIER.getHeight()/MOVESPEED) {
			time += Gdx.graphics.getDeltaTime();
			pos.y += Gdx.graphics.getDeltaTime()*MOVESPEED;
		} else {
			pos.y = Game.WORLDH  - ter.getHeight((int)pos.x + TANKBARRIER.getWidth()/2) - 4;
		}
	}
	
	public void render(SpriteBatch Batch, Camera Cam)
	{
		Batch.setColor(Terrain.getColor());
		Batch.draw(TANKBARRIER, Cam.getRenderX(pos.x), Cam.getRenderY(pos.y));
		Batch.setColor(Color.WHITE);
	}
}
