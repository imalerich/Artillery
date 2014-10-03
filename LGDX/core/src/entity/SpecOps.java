package entity;

import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.AnimTex;
import com.mygdx.game.Game;
import com.mygdx.game.Shaders;

public class SpecOps extends Unit
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
	
	public SpecOps(Terrain Ter, Vector2 Pos, int Speed)
	{
		if (anim == null) {
			spritesheet = new Texture( Gdx.files.internal("img/specops.png") );
			
			anim = new AnimTex(spritesheet, 1, 2, 2);
			anim.NewAnimation(0, 1, 0, 0, 0.0f);
			anim.NewAnimation(1, 2, 0, 1, 0.2f);
		}
		
		halfwidth = anim.GetFrameWidth();
		
		pos = Pos;
		pos.y = Game.WORLDH - Ter.GetHeight((int)pos.x+halfwidth) - 3;
		
		width = anim.GetFrameWidth();
		height = anim.GetFrameHeight();
		
		forward = true;
		ter = Ter;
		speed = Speed;
	}
	
	private void DrawHighlight(SpriteBatch Batch, Vector2 Campos)
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
				
				anim.Render(Batch, 1, hpos, 1.0f, 1.0f);
			}
		}
		
		Batch.setShader(null);
	}
	
	public void Draw(SpriteBatch Batch, Vector2 Campos, boolean Highlight)
	{
		Vector2 Coords = new Vector2(pos);
		Coords.x -= Campos.x;
		Coords.y -= Campos.y;
		
		if (moving)
			anim.UpdateClock();
		
		if (Highlight)
			DrawHighlight(Batch, Campos);
		anim.Render(Batch, 1, Coords, 1.0f, 1.0f);
		moving = false;
	}
}
