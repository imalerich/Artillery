package entity;

import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Game;

public class WarPlane extends ObjectData
{
	private static Texture tex;
	private int height;
	private int minheight;
	private float theta;
	
	// because the texture is static, the dimmensions will also be static
	private static int halfwidth;
	private static int planewidth;
	private static int planeheight;
	
	private static final int TURNRATE = 90;
	
	public void Release()
	{
		tex.dispose();
	}
	
	public WarPlane(Terrain Ter, int Speed, int Height, int MinHeight)
	{
		if (tex == null)
			tex = new Texture( Gdx.files.internal("plane.png") );
		
		halfwidth = tex.getWidth()/2;
		planewidth = tex.getWidth();
		planeheight = tex.getHeight();
		
		pos = new Vector2();
		width = tex.getWidth();
		forward = true;
		ter = Ter;
		speed = Speed;
		height = Height;
		minheight = MinHeight;
		theta = 0.0f;
	}
	
	public void MoveRight()
	{
		super.MoveRight();
		
		// overwrite the height set by the super class MoveRight()
		if ( minheight + Game.WORLDH - ter.GetHeight((int)pos.x + halfwidth) > height )
			pos.y = minheight + Game.WORLDH - ter.GetHeight((int)pos.x + halfwidth);
		else pos.y = height;
	}
	
	public void MoveLeft()
	{
		super.MoveLeft();
		
		// overwrite the height set by the super class MoveLeft()
		if ( minheight + Game.WORLDH - ter.GetHeight((int)pos.x + halfwidth) > height )
			pos.y = minheight + Game.WORLDH - ter.GetHeight((int)pos.x + halfwidth);
		else pos.y = height;
	}
	
	private float GetAngle()
	{
		float phi = 0.0f;
		float h0 = ter.GetHeight( (int)pos.x+(halfwidth/2) );
		float h1 = ter.GetHeight( (int)pos.x+(halfwidth/2)+halfwidth );
		
		phi = -(float)Math.atan( (h1-h0)/(float)halfwidth );
		return (float)Math.toDegrees(phi);
	}
	
	public void Draw(SpriteBatch Batch, Vector2 Campos)
	{
		
		// get the target angle for theta
		float phi = 0.0f;
		if ( minheight + Game.WORLDH - ter.GetHeight((int)pos.x) > height )
			phi = GetAngle();
		
		// move the plane towards phi
		if (phi > theta)
			theta += Gdx.graphics.getDeltaTime()*TURNRATE;
		else theta -= Gdx.graphics.getDeltaTime()*TURNRATE;
		
		// draw the tank
		Batch.draw(tex, pos.x - Campos.x, pos.y - Campos.y, halfwidth, 0, planewidth, planeheight, 1.0f, 1.0f, 
				theta, 0, 0, planewidth, planeheight, !forward, false);
	}
}
