package arsenal;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;

public class CannonBall extends Projectile
{
	private static Texture tex;
	
	public static void Release()
	{
		if (tex != null)
			tex.dispose();
	}
	
	private void LoadTex()
	{
		if (tex == null)
			tex = new Texture( Gdx.files.internal("img/weaponry/cannonball.png") );
		
		// set the radius of this projectile
		radius = tex.getWidth()/2;
		blastradius = radius*4;
		ignoreGravity = false;
	}
	
	public CannonBall(Vector2 Position, Vector2 Velocity)
	{
		LoadTex();
		
		pos = Position;
		vel = Velocity;
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam)
	{
		Batch.draw(tex, Cam.GetRenderX(pos.x), Cam.GetRenderY(pos.y));
	}
}
