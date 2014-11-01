package physics;

import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;

import entity.Tank;

public class Missile 
{
	public static final Color COLOR = new Color(128/255f, 128/255f, 128/255f, 1f);
	private static final int GRAVITY = 144/2; // px's per second
	private static Texture tex;
	
	private Terrain ter;
	private Vector2 pos;
	private Vector2 vel;
	private boolean hashit;
	
	public static void Init()
	{
		if (tex == null) {
			tex = new Texture(Gdx.files.internal("img/weaponry/missile.png"));
		}
	}
	
	public static void Release()
	{
		if (tex != null) {
			tex.dispose();
		}
	}
	
	public Missile(Terrain Ter, Vector2 Source, Vector2 Velocity)
	{
		ter = Ter;
		pos = Source;
		vel = Velocity;
		hashit = false;
		
		// offset the position by a the velocity by the length of the barrel
		Vector2 tmp = new Vector2(vel);
		tmp.nor();
		
		pos.x += tmp.x*Tank.GetBarrelWidth();
		pos.y += tmp.y*Tank.GetBarrelWidth();
	}
	
	public void Update()
	{
		if (hashit) {
			return;
		}
		
		// apply gravity to the velocity
		vel.y -= GRAVITY * Gdx.graphics.getDeltaTime();
		
		// update the position
		pos.x += vel.x;
		pos.y += vel.y;
		
		// wrap the x position
		if (pos.x >= Game.WORLDW) {
			pos.x -= Game.WORLDW;
		} else if (pos.x < 0) {
			pos.x += Game.WORLDW;
		}
		
		if (ter.Contains(pos.x, pos.y)) {
			ter.CutHole((int)pos.x, Game.WORLDH - (int)pos.y, 64);
			hashit = true;
		}
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam)
	{
		if (hashit) {
			return;
		}
		
		// the angle to draw the missile at
		float theta = (float)Math.toDegrees( Math.atan( vel.y/vel.x ) );
		if (vel.x < 0) {
			theta += 180f;
		}
		
		Batch.setColor(COLOR);
		Batch.draw(tex, Cam.GetRenderX(pos.x + tex.getWidth()/2f), Cam.GetRenderY(pos.y + tex.getHeight()/2f), 
				0, 0, tex.getWidth(), tex.getHeight(),
				1f, 1f, theta, 0, 0, tex.getWidth(), tex.getHeight(), false, false);
		Batch.setColor(Color.WHITE);
	}
	
	public boolean HasHit()
	{
		return hashit;
	}
}
