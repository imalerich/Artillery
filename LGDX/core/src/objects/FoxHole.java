package objects;

import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;

public class FoxHole 
{
	public static final float MOVESPEED = 64;
	public static final int MOUNDWIDTH = 29;
	public static Texture FOXHOLE;
	private final Vector2 pos;
	private float targety;
	
	public static void Init()
	{
		FOXHOLE = new Texture( Gdx.files.internal("img/objects/foxhole.png") );
	}
	
	public static void Release()
	{
		if (FOXHOLE == null) 
			FOXHOLE.dispose();
	}
	
	public FoxHole(Vector2 Pos)
	{
		pos = Pos;
		
		// set the mound lower and have it rise out of the ground
		targety = pos.y;
		pos.y -= FOXHOLE.getHeight();
	}
	
	public void Update()
	{
		if (pos.y < targety) {
			pos.y += Gdx.graphics.getDeltaTime()*MOVESPEED;
		} else if (pos.y > targety) {
			pos.y = targety;
		}
	}
	
	public void Render(SpriteBatch Batch, Camera Cam)
	{
		Batch.setColor(Terrain.GetColor());
		Batch.draw(FOXHOLE, Cam.GetRenderX(pos.x), Cam.GetRenderY(pos.y));
		Batch.setColor(Color.WHITE);
	}
}
