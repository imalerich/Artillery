package entity;

import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.AnimTex;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;
import com.mygdx.game.Shaders;

public class StealthTroop extends Unit
{
	private AnimTex anim;
	private static Texture spritesheet;
	private static int halfwidth;
	
	public void Release()
	{
		if (spritesheet != null)
			spritesheet.dispose();
		
		anim.Release();
	}
	
	public StealthTroop(Terrain Ter, Vector2 Pos, int Speed)
	{
		if (anim == null) {
			spritesheet = new Texture( Gdx.files.internal("img/units/stealthtroops.png") );
			
			anim = new AnimTex(spritesheet, 1, 2, 2);
			anim.NewAnimation(0, 1, 0, 0, 0.0f);
			anim.NewAnimation(1, 2, 0, 1, 0.2f);
		}
		
		halfwidth = anim.GetFrameWidth();
		
		mugshotIndex = 1;
		viewRadius = 512;
		pos = Pos;
		pos.y = Game.WORLDH - Ter.GetHeight((int)pos.x+halfwidth) - 3;
		
		width = anim.GetFrameWidth();
		height = anim.GetFrameWidth();
		
		forward = true;
		ter = Ter;
		speed = Speed;
	}
	
	private void DrawOutline(SpriteBatch Batch, Camera Cam)
	{
		for (int x=-1; x<2; x++) {
			for (int y=-1; y<2; y++) {
				Vector2 Coords = new Vector2(pos);
				Coords.x += x;
				Coords.y += y;
				
				if (forward)
					anim.Render(Batch, Cam, 1, Coords, 1.0f, 1.0f);
				else anim.Render(Batch, Cam, 1, Coords, -1.0f, 1.0f);
			}
		}
	}
	
	private void DrawTarget(SpriteBatch Batch, Camera Cam)
	{
		Shaders.SetShader(Batch, Shaders.target);
		DrawOutline(Batch, Cam);
		Shaders.RevertShader(Batch);
	}
	
	private void DrawHighlight(SpriteBatch Batch, Camera Cam)
	{
		Shaders.SetShader(Batch, Shaders.hili);
		DrawOutline(Batch, Cam);
		Shaders.RevertShader(Batch);
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam, boolean Highlight, boolean Target)
	{
		Vector2 Coords = new Vector2(pos);
		
		if (moving)
			anim.UpdateClock();
		
		if (Highlight)
			DrawHighlight(Batch, Cam);
		else if (Target)
			DrawTarget(Batch, Cam);
		
		if (forward)
			anim.Render(Batch, Cam, 1, Coords, 1.0f, 1.0f);
		else anim.Render(Batch, Cam, 1, Coords, -1.0f, 1.0f);
		moving = false;
	}
}
