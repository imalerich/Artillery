package entity;

import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Game;
import com.mygdx.game.Shaders;

public class WarPlane extends Unit
{
	private static Texture tex;
	private int flightheight;
	private int minheight;
	private float theta;
	
	// because the texture is static, the dimmensions will also be static
	private static int halfwidth;
	
	private static final int TURNRATE = 90;
	
	public void Release()
	{
		tex.dispose();
	}
	
	public WarPlane(Terrain Ter, int Speed, int Height, int MinHeight)
	{
		if (tex == null)
			tex = new Texture( Gdx.files.internal("img/plane.png") );
		
		halfwidth = tex.getWidth()/2;
		width = tex.getWidth();
		height = tex.getHeight();
		
		pos = new Vector2();
		forward = true;
		ter = Ter;
		speed = Speed;
		flightheight = Height;
		minheight = MinHeight;
		theta = 0.0f;
	}
	
	public void MoveRight()
	{
		super.MoveRight();
		
		// overwrite the height set by the super class MoveRight()
		if ( minheight + Game.WORLDH - ter.GetHeight((int)pos.x + halfwidth) > flightheight )
			pos.y = minheight + Game.WORLDH - ter.GetHeight((int)pos.x + halfwidth);
		else pos.y = flightheight;
	}
	
	public void MoveLeft()
	{
		super.MoveLeft();
		
		// overwrite the height set by the super class MoveLeft()
		if ( minheight + Game.WORLDH - ter.GetHeight((int)pos.x + halfwidth) > flightheight )
			pos.y = minheight + Game.WORLDH - ter.GetHeight((int)pos.x + halfwidth);
		else pos.y = flightheight;
	}
	
	private float GetAngle()
	{
		float phi = 0.0f;
		float h0 = ter.GetHeight( (int)pos.x+(halfwidth/2) );
		float h1 = ter.GetHeight( (int)pos.x+(halfwidth/2)+halfwidth );
		
		phi = -(float)Math.atan( (h1-h0)/(float)halfwidth );
		return (float)Math.toDegrees(phi);
	}
	
	public void DrawHighlight(SpriteBatch Batch, Vector2 Campos)
	{
		// draw a highlighted version of the sprite
		Batch.setShader(Shaders.hili);
		
		Vector2 Coords = new Vector2(pos);
		Coords.x -= Campos.x;
		Coords.y -= Campos.y;
		
		for (int x=-1; x<2; x++) {
			for (int y=-1; y<2; y++) {
				Vector2 hpos = new Vector2(Coords);
				hpos.x += x;
				hpos.y += y;
				
				Batch.draw(tex, hpos.x, hpos.y, halfwidth, 0, width, height, 1.0f, 1.0f, 
						theta, 0, 0, width, height, !forward, false);
			}
		}
		
		Batch.setShader(null);
	}
	
	public void Draw(SpriteBatch Batch, Vector2 Campos, boolean Highlight)
	{
		
		// get the target angle for theta
		float phi = 0.0f;
		if ( minheight + Game.WORLDH - ter.GetHeight((int)pos.x) > flightheight )
			phi = GetAngle();
		
		// move the plane towards phi
		if (phi > theta)
			theta += Gdx.graphics.getDeltaTime()*TURNRATE;
		else theta -= Gdx.graphics.getDeltaTime()*TURNRATE;
		
		// draw the plane 
		if (Highlight)
			DrawHighlight(Batch, Campos);
		Batch.draw(tex, pos.x - Campos.x, pos.y - Campos.y, halfwidth, 0, width, height, 1.0f, 1.0f, 
				theta, 0, 0, width, height, !forward, false);
	}
}
