package physics;

import particles.Particles;
import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;

public class Grenade extends Missile 
{
	private static final int ROTTIME = 180;
	private static Texture tex;
	private static float phi;
	
	public static void init()
	{
		if (tex == null)
			tex = new Texture("img/weaponry/grenade.png");
	}
	
	public static void release()
	{
		if (tex != null)
			tex.dispose();
	}
	
	public Grenade(GameWorld GW, Terrain Ter, Particles Particle, Vector2 Source, Vector2 Velocity, 
			float Strength, int SourceArmy, int Bounces, int BlastRadius, boolean Incinerate) 
	{
		super(GW, Ter, Particle, Source, Velocity, Strength, SourceArmy, Bounces, BlastRadius, 0, 0, Incinerate);
		dusttime = 0.3f;
		dustdecay = 1f;
		dustspeed = 256.0;
		postdustr = 8f;
		
		phi = 0f;
	}
	
	@Override
	protected void addParticle()
	{
		//
	}
	
	@Override
	protected void playSound()
	{
		//
	}
	
	@Override
	protected void addKick(Camera Cam)
	{
		//
	}
	
	@Override
	protected void procBlast(float blastScale)
	{
		gw.procBlast( new Blast(pos, blastRadius, strength, sourceArmy));
	}
	
	@Override
	public void draw(SpriteBatch Batch, Camera Cam)
	{
		if (hashit) {
			return;
		}
		
		// spin while being thrown
		phi += Gdx.graphics.getDeltaTime() * ROTTIME;
		Batch.draw(tex, Cam.getRenderX(pos.x + tex.getWidth()/2f), Cam.getRenderY(pos.y + tex.getHeight()/2f), 
				0, 0, tex.getWidth(), tex.getHeight(),
				1f, 1f, phi, 0, 0, tex.getWidth(), tex.getHeight(), false, false);
	}
}
