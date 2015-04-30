package physics;

import particles.Particles;
import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.AnimTex;
import com.mygdx.game.Camera;

public class Flame 
{
	public static final int FLAMEDMG = 2;
	public static final int ONFIRETURNS = 2;
	private static final int PPS = 100; // particles per second
	
	private static final int ANIMCOUNT = 3;
	private static AnimTex flame;
	private final Particles particles;
	private final Terrain ter;
	public final int source;
	public final float forward;
	public final Vector2 pos;
	public final Polygon bounding;
	public final int strength;
	private float time = 0f;
	private float parttime = 0f;
	
	private int index = 0;
	private int count = 0;
		
	public static void init()
	{
		flame = new AnimTex("img/weaponry/flame_anim.png", 1, 17, 3);
		flame.newAnimation(0, 6, 0, 5, 0.04f);
		flame.newAnimation(1, 5, 6, 10, 0.04f);
		flame.newAnimation(2, 6, 11, 16, 0.04f);
	}
	
	public static void release()
	{
		flame.release();
	}
	
	public Flame(Terrain Ter, Particles Part, Vector2 SourcePos, boolean Forward, int SourceArmy, int Strength)
	{
		if (Forward)
			forward = 1f;
		else
			forward = -1f;
		
		ter = Ter;
		particles = Part;
		source = SourceArmy;
		strength = Strength;
		pos = new Vector2(SourcePos.x - 7, SourcePos.y - 30);
		
		float[] vertices = new float[6];
		Vector2 v0 = new Vector2(SourcePos.x, SourcePos.y);
		Vector2 v1 = new Vector2(forward*flame.getFrameWidth()*(4/5f)-7, -30f);
		Vector2 v2 = new Vector2(forward*flame.getFrameWidth()*(4/5f)-7, flame.getFrameHeight()-30f);
		v1.rotate( getAngle() );
		v2.rotate( getAngle() );
		v1.x += v0.x;
		v1.y += v0.y;
		v2.x += v0.x;
		v2.y += v0.y;
		
		vertices[0] = v0.x;
		vertices[1] = v0.y;
		vertices[2] = v1.x;
		vertices[3] = v1.y;
		vertices[4] = v2.x;
		vertices[5] = v2.y;
		
		bounding = new Polygon(vertices);
	}
	
	public boolean isAlive()
	{
		return (index != 3);
	}
	
	public void update(Camera Cam)
	{
		if (!isAlive())
			return;
		
		flame.setTime(time);
		
		if (flame.isCompleted(index)) {
			switch (index) {
			case 0:
				time = (float)Math.random();
				flame.setTime(time);
				index = 1;
				break;
				
			case 1:
				if (count < ANIMCOUNT) {
					count++;
					index = 1;
					time = 0f;
					flame.setTime(time);
				} else {
					time = 0f;
					flame.setTime(0f);
					index = 2;
				}
				
				break;
				
			case 2:
				time = 0f;
				index = 3;
				break;
			}
		}
		
		addParticles();
	}
	
	public void draw(SpriteBatch Batch, Camera Cam)
	{
		if (!isAlive())
			return;
		
		boolean looping = false;
		if (index == 1)
			looping = true;
		
		TextureRegion tex = flame.getCurrent(index, looping);
		Batch.draw(tex, Cam.getRenderX(pos.x), Cam.getRenderY(pos.y), 7, 30, tex.getRegionWidth(), tex.getRegionHeight(), forward, 1f, getAngle());
		
		time += Gdx.graphics.getDeltaTime();
	}
	
	private float getAngle()
	{
		float x0 = ter.getHeight((int)pos.x);
		float x1 = ter.getHeight((int)(pos.x + forward*flame.getFrameWidth()*0.75f));
		
		double theta = forward * -Math.atan( (x1-x0)/(flame.getFrameWidth()) );
		return (float)Math.toDegrees(theta);
	}
	
	private void addParticles()
	{
		parttime += Gdx.graphics.getDeltaTime();
		
		int addcount = (int)(PPS*parttime);
		if (addcount == 0) {
			return;
		}
		
		for (int i=0; i<addcount; i++) {
			float radius = (float)Math.random()*6 + 6;
			Vector2 v = new Vector2((float)Math.random()*16, (float)Math.random()*16);
			v.x += forward * Math.random()*32;
			
			float quarterwidth = flame.getFrameWidth()/4f;
			float halfwidth = flame.getFrameWidth()/2f;
			
			float xpos = pos.x + (float)(Math.random()*halfwidth) + quarterwidth;
			float ypos = pos.y + flame.getFrameHeight()/2f;
			float radmod = ( ((xpos - pos.x - quarterwidth)/halfwidth) );
			if (forward == -1f)
				radmod = -radmod + 1f;
			radius *= ( radmod + 1f );
			
			if (forward == -1)
				xpos  -= flame.getFrameWidth();
			
			float xoff = xpos - (pos.x + 7);
			float yoff = (float)Math.tan( Math.toRadians(getAngle()) ) * xoff;
			ypos += yoff;
			
			
			if (index == 0)
				radius *= Math.min(time, 1f);
			else if (index == 2)
				radius *= Math.max(-time + 1f, 0f);
				
			
			particles.addParticle(radius, new Vector2(xpos, ypos), v);
		}
		
		parttime = 0.0f;
	}
}
