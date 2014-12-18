package objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;

public class RadioTower 
{
	public static Texture Tower;
	private final Vector2 pos;
	
	public static void init()
	{
		if (Tower == null)
			Tower = new Texture( Gdx.files.internal("img/objects/tower.png") );
	}
	
	public static void release()
	{
		if (Tower != null)
			Tower.dispose();
	}
	
	public RadioTower(Vector2 Pos)
	{
		pos = Pos;
	}
	
	public void draw(SpriteBatch Batch, Camera Cam)
	{
		Batch.draw(Tower, Cam.getRenderX(pos.x), Cam.getRenderY(pos.y));
	}
}
