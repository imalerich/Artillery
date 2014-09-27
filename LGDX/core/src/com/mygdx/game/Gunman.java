package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Gunman extends Entity
{
	private AnimTex anim;
	
	public void Release()
	{
		anim.Release();;
	}
	
	public Gunman(Terrain Ter, Vector2 Pos, int Speed)
	{
		if (anim == null) {
			anim = new AnimTex("gunman.png", 1, 2, 2);
			anim.NewAnimation(0, 1, 0, 0, 0.0f);
			anim.NewAnimation(1, 2, 0, 1, 0.2f);
		}
		
		pos = Pos;
		pos.y = Game.WORLDH - Ter.GetHeight((int)pos.x+32) - 3;
		
		width = anim.GetFrameWidth();
		forward = true;
		ter = Ter;
		speed = Speed;
	}
	
	public void Draw(SpriteBatch Batch, Vector2 Campos)
	{
		Vector2 Coords = new Vector2(pos);
		Coords.x -= Campos.x;
		Coords.y -= Campos.y;
		
		anim.Render(Batch, 1, Coords, 1.0f, 1.0f);
	}
}