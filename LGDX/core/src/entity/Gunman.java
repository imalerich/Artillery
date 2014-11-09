package entity;

import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.AnimTex;
import com.mygdx.game.Camera;
import com.mygdx.game.Cursor;
import com.mygdx.game.Game;
import com.mygdx.game.Shaders;

public class Gunman extends Unit
{
	public static Texture DEATHANIM;
	public static Texture SPRITESHEET;
	
	private AnimTex death;
	private AnimTex anim;
	private static int halfwidth;
	private float lasthealth;
	private double dmgclock;
	
	public static void Init()
	{
		SPRITESHEET = new Texture( Gdx.files.internal("img/units/gunman.png") );
		DEATHANIM = new Texture( Gdx.files.internal("img/units/deathanim.png") );
	}
	
	public void Release()
	{
		if (SPRITESHEET != null)
			SPRITESHEET.dispose();
		
		if (DEATHANIM != null)
			DEATHANIM.dispose();
		
		anim.Release();
	}
	
	public boolean IsAlive()
	{
		return !death.IsCompleted(0);
	}
	
	public Gunman(Terrain Ter, Vector2 Pos, int Speed)
	{
		if (anim == null) {
			anim = new AnimTex(SPRITESHEET, 1, 4, 4);
			anim.NewAnimation(0, 1, 0, 0, 0.0f);
			anim.NewAnimation(1, 2, 0, 1, 0.2f);
			anim.NewAnimation(2, 1, 2, 2, 0.0f);
			anim.NewAnimation(3, 1, 3, 3, 0.0f);
		}
		
		if (death == null) {
			death = new AnimTex(DEATHANIM, 1, 3, 1);
			death.NewAnimation(0, 3, 0, 2, 0.1f);
			death.SetTime(0.0f);
		}
		
		halfwidth = anim.GetFrameWidth();
		
		pos = Pos;
		pos.y = Game.WORLDH - Ter.GetHeight((int)pos.x+halfwidth) - 3;
		
		width = anim.GetFrameWidth();
		height = anim.GetFrameHeight();
		
		forward = true;
		ter = Ter;
		speed = Speed;
		mugshotIndex = 0;
		health = 10;
		maxhealth = 10;
		lasthealth = health;
		dmgclock = 0f;
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
	
	private void DrawHighlight(SpriteBatch Batch, Camera Cam, int Index)
	{
		// draw a highlighted version of the sprite
		Shaders.SetShader(Batch, Shaders.hili);
		DrawOutline(Batch, Cam, Index);
		Shaders.RevertShader(Batch);
	}
	
	private void DrawTarget(SpriteBatch Batch, Camera Cam, int Index)
	{
		Shaders.SetShader(Batch, Shaders.target);
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
		} else if (lasthealth != health) {
			dmgclock += Gdx.graphics.getDeltaTime();
			index = 3;
			
			if (dmgclock > 0.4f) {
				lasthealth = health;
				dmgclock = 0f;
			}
		}
		
		if (moving)
			anim.UpdateClock();
		
		if (Highlight)
			DrawHighlight(Batch, Cam, index);
		else if (Target)
			DrawTarget(Batch, Cam, index);
		
		int width = anim.GetFrameWidth();
		int height = anim.GetFrameHeight();
		DrawAnim(Batch, Cam, index, Coords, width, height);
		
		if (Cursor.IsMouseOver(GetBBox(), Cam.GetPos())) {
			Shaders.SetShader(Batch, Shaders.health);
			int h = (int)(height * (float)health/maxhealth);
			DrawAnim(Batch, Cam, index, Coords, width, h);
			Shaders.RevertShader(Batch);
		}
		
		moving = false;
	}
	
	private void DrawAnim(SpriteBatch Batch, Camera Cam, int Index, Vector2 Coords, int SrcWidth, int SrcHeight)
	{
		if (forward)
			anim.Render(Batch, Cam, Index, Coords, 1.0f, 1.0f, true, SrcWidth, SrcHeight);
		else anim.Render(Batch, Cam, Index, Coords, -1.0f, 1.0f, true, SrcWidth, SrcHeight);
	}
}
