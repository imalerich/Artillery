package particles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;

public class Ember 
{
	public static final int GRAVITY = 40;
	public static Texture tex;
	private final float DECAY;
	
	private float scale;
	private Vector2 pos;
	private Vector2 vel;
	private Vector2 slowrate;
	private boolean isalive = true;
	
	public static void init()
	{
		if (tex == null)
			tex = new Texture( Gdx.files.internal("img/particles/fire.png") );
	}
	
	public static void release()
	{
		if (tex != null)
			tex.dispose();
	}
	
	public Ember(float Scale, Vector2 Pos, Vector2 Vel, float SlowTime, float Decay)
	{
		scale = Scale;
		pos = new Vector2(Pos);
		vel = new Vector2(Vel);
		DECAY = Decay;
		
		if (SlowTime != 0f)	 {
			slowrate = new Vector2(vel.x/SlowTime, vel.y/SlowTime);
			slowrate.x = Math.abs(slowrate.x);
			slowrate.y = Math.abs(slowrate.y);
		}
	}
	
	public boolean isAlive()
	{
		return isalive;
	}
	
	public void update()
	{
		modVel();
		pos.x += vel.x * Gdx.graphics.getDeltaTime();
		pos.y += vel.y * Gdx.graphics.getDeltaTime();
		pos.y += GRAVITY * Gdx.graphics.getDeltaTime();
		
		if (pos.x >= Game.WORLDW)
			pos.x -= Game.WORLDW;
		else if (pos.x < 0)
			pos.x += Game.WORLDW;
		
		scale -= Gdx.graphics.getDeltaTime() * DECAY;
		if (scale < 0f)
			scale = 0f;
		
		if (scale <= 0f)
			isalive = false;
	}
	
	public void draw(SpriteBatch Batch, Camera Cam)
	{
		Batch.draw(tex, Cam.getRenderX(pos.x), Cam.getRenderY(pos.y), tex.getWidth()*scale, tex.getHeight()*scale);
	}
	
	private void modVel()
	{
		float time = Gdx.graphics.getDeltaTime();
		if (vel.x > 0) {
			vel.x -= slowrate.x * time;
			if (vel.x < 0) {
				vel.x = 0f;
			}
			
		} else if (vel.x < 0) {
			vel.x += slowrate.x * time;
			if (vel.x > 0) {
				vel.x = 0f;
			}
		}
		
		if (vel.y > 0) {
			vel.y -= slowrate.y * time;
			if (vel.y < 0) {
				vel.y = 0f;
			}
			
		} else if (vel.y < 0) {
			vel.y += slowrate.y * time;
			if (vel.y > 0) {
				vel.y = 0f;
			}
		}
	}
}
