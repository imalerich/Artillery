package ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;

public class ReqIndicator 
{
	private static Texture tex;
	private final int value;
	private final float lifespan;
	
	private Vector2 pos;
	private Vector2 vel;
	private float time;
	
	public static void init()
	{
		if (tex == null)
			tex = new Texture( Gdx.files.internal("img/ui/indicators/req_indicator.png") );
	}
	
	public static void release()
	{
		if (tex != null)
			tex.dispose();
	}
	
	public ReqIndicator(Vector2 Pos, Vector2 Vel, int Value, float LifeSpan)
	{
		pos = new Vector2(Pos);
		pos.x -= tex.getWidth()/2f;
		vel = new Vector2(Vel);
		
		value = Value;
		lifespan = LifeSpan;
		time = 0f;
	}
	
	public boolean isAlive()
	{
		return (time < lifespan);
	}
	
	public void update()
	{
		time += Gdx.graphics.getDeltaTime();
		pos.x += ( vel.x * Gdx.graphics.getDeltaTime() );
		pos.y += ( vel.y * Gdx.graphics.getDeltaTime() );
	}
	
	public void draw(SpriteBatch Batch, Camera Cam)
	{
		if (value == 0)
			return;
		
		// draw the background texture
		float alpha = 1f - (float)Math.pow( Math.min((time/lifespan), 1f), 4 );
		
		Batch.setColor(1f, 1f, 1f, alpha);
		Batch.draw(tex, Cam.getRenderX(pos.x), Cam.getRenderY(pos.y));
		
		// draw the value
		if (value < 0)
			Batch.setColor(1f, 0f, 0f, alpha);
		else
			Batch.setColor(0f, 1f, 0f, alpha);
		
		drawVal(Batch, Cam);
		Batch.setColor(Color.WHITE);
	}
	
	private void drawVal(SpriteBatch Batch, Camera Cam)
	{
		boolean negative = false;
		float xpos = Cam.getRenderX(pos.x + tex.getWidth()/2f);
		float ypos = Cam.getRenderY(pos.y + 4);
		int val = value;
		int i=0;
		
		if (val < 0) {
			negative = true;
			val = Math.abs(val);
		}
		
		while (true) {
			int digit = val % 10;
			if (val == 0 && i != 0) {
				if (negative) {
					digit = MenuBar.CHARSET.length-1;
					Batch.draw(MenuBar.CHARSET[digit], xpos - i*MenuBar.CHARSET[digit].getRegionWidth()-i, 
							ypos);
					i++;
				}
				
				break;
			}
			
			Batch.draw(MenuBar.CHARSET[digit], xpos - i*MenuBar.CHARSET[digit].getRegionWidth()-i, ypos);

			
			val -= digit;
			val /= 10;
			i++;
		}
	}
}
