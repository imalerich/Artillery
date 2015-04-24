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

import entity.Squad;

public class FoxHole 
{
	public static final float MOVESPEED = 64;
	public static final int MOUNDWIDTH = 29;
	public static Texture FOXHOLE;
	
	private final Vector2 pos;
	private float targety;
	private Squad isoccupied;
	private Rectangle bbox;
	
	public static void init()
	{
		FOXHOLE = new Texture( Gdx.files.internal("img/objects/foxhole.png") );
	}
	
	public static void release()
	{
		if (FOXHOLE == null) 
			FOXHOLE.dispose();
	}
	
	public FoxHole(Vector2 Pos)
	{
		pos = new Vector2(Pos);
		isoccupied = null;
		
		// set the mound lower and have it rise out of the ground
		targety = pos.y;
		pos.y -= FOXHOLE.getHeight();
		bbox = new Rectangle(pos.x, 0, FOXHOLE.getWidth(), Game.WORLDH);
	}
	
	public Vector2 getPos()
	{
		return pos;
	}
	
	public Rectangle getBBox()
	{
		return bbox;
	}
	
	public Squad getOccupied()
	{
		return isoccupied;
	}
	
	public boolean isOccupied()
	{
		return isoccupied != null;
	}
	
	public void setOccupied(Squad Occupied)
	{
		isoccupied = Occupied;
	}
	
	public void update()
	{
		if (pos.y < targety) {
			pos.y += Gdx.graphics.getDeltaTime()*MOVESPEED;
		} else if (pos.y > targety) {
			pos.y = targety;
		}
	}
	
	public void render(SpriteBatch Batch, Camera Cam)
	{
		Batch.setColor(Terrain.getColor());
		Batch.draw(FOXHOLE, Cam.getRenderX(pos.x), Cam.getRenderY(pos.y));
		Batch.setColor(Color.WHITE);
	}
}
