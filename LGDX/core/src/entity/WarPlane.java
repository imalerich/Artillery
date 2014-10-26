package entity;

import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
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
			tex = new Texture( Gdx.files.internal("img/units/plane.png") );
		
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
	
	public float GetAngle()
	{
		float phi = 0.0f;
		float h0 = ter.GetHeight( (int)pos.x+(halfwidth/2) );
		float h1 = ter.GetHeight( (int)pos.x+(halfwidth/2)+halfwidth );
		
		phi = -(float)Math.atan( (h1-h0)/(float)halfwidth );
		return (float)Math.toDegrees(phi);
	}
	
	private void DrawOutline(SpriteBatch Batch, Camera Cam)
	{
		for (int x=-1; x<2; x++) {
			for (int y=-1; y<2; y++) {
				Vector2 Coords = new Vector2(pos);
				Coords.x += x;
				Coords.y += y;
				
				Batch.draw(tex, Cam.GetRenderX(Coords.x), Cam.GetRenderY(Coords.y),
						halfwidth, 0, width, height, 1.0f, 1.0f, 
						theta, 0, 0, width, height, !forward, false);
			}
		}
	}
	
	private void DrawTarget(SpriteBatch Batch, Camera Cam)
	{
		// draw a highlighted version of the sprite
		Shaders.SetShader(Batch, Shaders.target);
		DrawOutline(Batch, Cam);
		Shaders.RevertShader(Batch);
	}
	
	private void DrawHighlight(SpriteBatch Batch, Camera Cam)
	{
		// draw a highlighted version of the sprite
		Shaders.SetShader(Batch, Shaders.hili);
		DrawOutline(Batch, Cam);
		Shaders.RevertShader(Batch);
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam, boolean Highlight, boolean Target)
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
			DrawHighlight(Batch, Cam);
		else if (Target)
			DrawTarget(Batch, Cam);
		
		Batch.draw(tex, Cam.GetRenderX(pos.x), Cam.GetRenderY(pos.y),
				halfwidth, 0, width, height, 1.0f, 1.0f, 
				theta, 0, 0, width, height, !forward, false);
	}
}
