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
	private AnimTex death;
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
			
			anim = new AnimTex(spritesheet, 1, 3, 3);
			anim.NewAnimation(0, 1, 0, 0, 0.0f);
			anim.NewAnimation(1, 2, 0, 1, 0.2f);
			anim.NewAnimation(2, 1, 2, 2, 0.0f);
		}
		
		if (death == null) {
			death = new AnimTex(Gunman.DEATHANIM, 1, 3, 1);
			death.NewAnimation(0, 3, 0, 2, 0.1f);
			death.SetTime(0.0f);
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
		health = 10;
	}
	
	public boolean IsAlive()
	{
		return !death.IsCompleted(0);
	}
	
	private void DrawOutline(SpriteBatch Batch, Camera Cam, int Index)
	{
		for (int x=-1; x<2; x++) {
			for (int y=-1; y<2; y++) {
				Vector2 Coords = new Vector2(pos);
				Coords.x += x;
				Coords.y += y;
				
				if (forward)
					anim.Render(Batch, Cam, Index, Coords, 1.0f, 1.0f);
				else anim.Render(Batch, Cam, Index, Coords, -1.0f, 1.0f);
			}
		}
	}
	
	private void DrawTarget(SpriteBatch Batch, Camera Cam, int Index)
	{
		Shaders.SetShader(Batch, Shaders.target);
		DrawOutline(Batch, Cam, Index);
		Shaders.RevertShader(Batch);
	}
	
	private void DrawHighlight(SpriteBatch Batch, Camera Cam, int Index)
	{
		Shaders.SetShader(Batch, Shaders.hili);
		DrawOutline(Batch, Cam, Index);
		Shaders.RevertShader(Batch);
	}
	
	public void DrawDieing(SpriteBatch Batch, Camera Cam)
	{
		death.UpdateClock();
		
		if (forward)
			death.Render(Batch, Cam, 0, pos, 1.0f, 1.0f, false);
		else
			death.Render(Batch, Cam, 0, pos, -1.0f, 1.0f, false);
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam, boolean Highlight, boolean Target)
	{
		if (health <= 0) {
			DrawDieing(Batch, Cam);
			return;
		}
		
		SetHeight();
		Vector2 Coords = new Vector2(pos);
		int index = 1;
		if (isFiring) {
			index = 2;
		}
		
		if (moving)
			anim.UpdateClock();
		
		if (Highlight)
			DrawHighlight(Batch, Cam, index);
		else if (Target)
			DrawTarget(Batch, Cam, index);
		
		if (forward)
			anim.Render(Batch, Cam, index, Coords, 1.0f, 1.0f);
		else anim.Render(Batch, Cam, index, Coords, -1.0f, 1.0f);
		moving = false;
	}
}
