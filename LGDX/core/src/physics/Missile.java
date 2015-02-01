package physics;

import particles.Particles;
import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;

import entity.Tank;

public class Missile 
{
	protected static final int PPS = 600; // particles per second
	protected final float DECAY = 0.6f;
	
	protected int gravity = 144*8; // px's per second
	protected float dusttime = 0.3f;
	protected float dustdecay = 1f;
	protected float postdustr = 32;
	protected double dustspeed = 512.0;
	
	protected float x0 = 0f;
	protected float x1 = 0f;
	protected float x2 = 0f;
	
	protected static Sound sfx;
	protected static Texture tex;
	
	protected Terrain ter;
	protected Particles particle;
	protected GameWorld gw;
	
	protected Vector2 pos;
	protected Vector2 vel;
	protected float strength;
	
	protected boolean hashit;
	protected double time;
	protected double totaltime;
	
	protected double posttime;
	protected double posttotaltime;
	
	protected boolean hasfired;
	protected final int sourceArmy;
	protected final int blastRadius;
	
	public static void init()
	{
		if (tex == null) {
			tex = new Texture(Gdx.files.internal("img/weaponry/missile.png"));
		}
		
		if (sfx == null) {
			sfx = Gdx.audio.newSound(Gdx.files.internal("aud/sfx/tankshot.wav"));
		}
	}
	
	public static void release()
	{
		if (tex != null) {
			tex.dispose();
		}
		
		if (sfx == null) {
			sfx.dispose();
		}
	}
	
	public Missile(GameWorld GW, Terrain Ter, Particles Particle, Vector2 Source, Vector2 Velocity, float Strength, int SourceArmy, int BlastRadius)
	{
		sourceArmy = SourceArmy;
		ter = Ter;
		gw = GW;
		particle = Particle;
		
		pos = new Vector2(Source);
		vel = new Vector2(Velocity);
		strength = Strength;
		hashit = false;
		
		// offset the position by a the velocity by the length of the barrel
		Vector2 tmp = new Vector2(vel);
		tmp.nor();
		
		pos.x += tmp.x*Tank.getBarrelWidth();
		pos.y += tmp.y*Tank.getBarrelWidth();
		
		// the amount of particles to add at the tanks barrel on launch relative to PPS
		time = 0.0;
		totaltime = 0.0;
		posttime = 0.0;
		posttotaltime = 0.0;
		blastRadius = BlastRadius;
		
		hasfired = false;
	}
	
	public void update(Camera Cam)
	{
		if (hashit) {
			addTerrainParticles();
			
			return;
		}
		
		if (!hasfired) {
			playSound();
			hasfired = true;
			
			time = 0.1f;
			addParticle();
			addKick(Cam);
		}
		
		// apply gravity to the velocity
		vel.y -= gravity * Gdx.graphics.getDeltaTime();
		
		addParticle();
		
		// update the position
		pos.x += vel.x * Gdx.graphics.getDeltaTime();
		pos.y += vel.y * Gdx.graphics.getDeltaTime();
		
		// wrap the x position
		if (pos.x >= Game.WORLDW) {
			pos.x -= Game.WORLDW;
		} else if (pos.x < 0) {
			pos.x += Game.WORLDW;
		}
		
		if (ter.contains(pos.x, pos.y) && !hashit) {
			hashit = true;
			
			// process the blast
			procBlast();
		}
	}
	
	protected void addKick(Camera Cam)
	{
		// get the distance from the point of fire to the center of the screen
		float dist = Vector2.dst(pos.x, pos.y, Cam.getPos().x + Game.SCREENW/2f, Cam.getPos().y + Game.SCREENH/2f);
		Vector2 tmp = new Vector2(vel);
		tmp.nor();

		float mag = 16;
		if (dist > Game.SCREENW)
			mag = 0f;
		else if (dist > 0f) {
			mag *= (Game.SCREENW-dist)/(Game.SCREENW);
		}

		Cam.addKick(-tmp.x*mag, -tmp.y*mag);
	}
	
	protected void procBlast()
	{
		x0 = Game.WORLDH - ter.getHeight((int)(pos.x - 16));
		x1 = Game.WORLDH - ter.getHeight((int)pos.x);
		x2 = Game.WORLDH - ter.getHeight((int)(pos.x + 16));
		
		gw.procBlast( new Blast(pos, blastRadius, strength, sourceArmy));
	}
	
	protected void playSound()
	{
		gw.getAudio().playSound(sfx, pos.x);
	}
	
	public void draw(SpriteBatch Batch, Camera Cam)
	{
		if (hashit) {
			return;
		}
		
		float theta = getTheta();
		Batch.draw(tex, Cam.getRenderX(pos.x + tex.getWidth()/2f), Cam.getRenderY(pos.y + tex.getHeight()/2f), 
				0, 0, tex.getWidth(), tex.getHeight(),
				1f, 1f, theta, 0, 0, tex.getWidth(), tex.getHeight(), false, false);
	}
	
	public boolean isCompleted()
	{
		return hashit && (posttotaltime > dusttime);
	}
	
	public boolean hasHit()
	{
		return hashit;
	}
	
	protected float getTheta()
	{
		// the angle to draw the missile at
		float theta = (float)Math.toDegrees( Math.atan( vel.y/vel.x ) );
		if (vel.x < 0) {
			theta += 180f;
		}
		
		return theta;
	}
	
	protected void addParticle()
	{
		totaltime += Gdx.graphics.getDeltaTime();
		time += Gdx.graphics.getDeltaTime();
		
		int addcount = (int)(PPS*time);
		if (addcount == 0) {
			return;
		}
		
		for (int i=0; i<addcount; i++) {
			float radius = (float)Math.random()*24 + 12;
			radius *= getRadiusMod();
			Vector2 v = new Vector2((float)Math.random()*16, (float)Math.random()*16);
			
			float xpos = pos.x + vel.x * (i/(float)addcount) * (float)time;
			float ypos = pos.y + vel.y * (i/(float)addcount) * (float)time;
			
			particle.addParticle(radius, new Vector2(xpos, ypos), v);
		}
		
		time = 0.0;
	}
	
	protected Vector2 getParticleVelocity(Vector2 V0, double Theta)
	{
		Vector2 v = new Vector2(V0);
		v = v.nor();

		v.x *= (dustspeed * getPostRadiusMod(time));
		v.y *= (dustspeed * getPostRadiusMod(time));
		v.rotate((int)(Theta));

		return v;
	}
	
	protected void addTerrainParticles()
	{
		double prevtot = posttotaltime;
		posttotaltime += Gdx.graphics.getDeltaTime();
		posttime += Gdx.graphics.getDeltaTime();
		
		if (posttotaltime > dusttime) {
			return;
		}
		
		int addcount = (int)(PPS*posttime*2);
		if (addcount == 0) {
			return;
		}
		
		Vector2 v0 = new Vector2(-16, x0-x1);
		Vector2 v1 = new Vector2(16, x2-x1);
		v0 = v0.nor();
		v1 = v1.nor();
		
		float theta = (float)Math.acos( Vector2.dot(v0.x, v0.y, v1.x, v1.y) );
		theta = -(float)Math.toDegrees(theta);
		if (theta > -100) {
			theta = -100;
		}
		
		for (int i=0; i<addcount; i++) {
			double time = prevtot + (float)(posttime) * (i/(float)addcount);
			float radius = (float)Math.random()*postdustr + postdustr;
			radius *= getPostRadiusMod(time);
			
			Vector2 v = getParticleVelocity(v0, Math.random()*theta);
		
			Vector2 p = new Vector2(pos);
			p.x += v.x * (float)(posttime) * (i/(float)addcount);
			p.y += v.y * (float)(posttime) * (i/(float)addcount);
			
			particle.addParticle(radius, p, v, dustdecay, 20f);
		}
		
		posttime = 0.0;
	}
	
	protected float getRadiusMod()
	{
		if (totaltime > DECAY) {
			return 0f;
		} else {
			return (DECAY - (float)totaltime)/DECAY;
		}
	}
	
	protected float getPostRadiusMod(double TotalTime)
	{
		if (TotalTime > dusttime) {
			return 0f;
		} else {
			return  (dusttime-(float)TotalTime)/dusttime;
		}
	}
}
