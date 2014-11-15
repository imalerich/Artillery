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
	private static final int GRAVITY = 144*8; // px's per second
	private static final int PPS = 600; // particles per second
	private static final float DECAY = 0.6f;
	
	private static final float DUSTTIME = 0.3f;
	private static final float DUSTDECAY = 1f;
	private static final double DUSTSPEED = 512.0;
	
	private static Sound sfx;
	private static Texture tex;
	
	private Terrain ter;
	private Particles particle;
	private Vector2 pos;
	private Vector2 vel;
	
	private boolean hashit;
	private double time;
	private double totaltime;
	
	private double posttime;
	private double posttotaltime;
	
	private boolean hasfired;
	
	public static void Init()
	{
		if (tex == null) {
			tex = new Texture(Gdx.files.internal("img/weaponry/missile.png"));
		}
		
		if (sfx == null) {
			sfx = Gdx.audio.newSound(Gdx.files.internal("aud/sfx/tankshot.wav"));
		}
	}
	
	public static void Release()
	{
		if (tex != null) {
			tex.dispose();
		}
		
		if (sfx == null) {
			sfx.dispose();
		}
	}
	
	public Missile(Terrain Ter, Particles Particle, Vector2 Source, Vector2 Velocity)
	{
		ter = Ter;
		particle = Particle;
		pos = Source;
		vel = Velocity;
		hashit = false;
		
		// offset the position by a the velocity by the length of the barrel
		Vector2 tmp = new Vector2(vel);
		tmp.nor();
		
		pos.x += tmp.x*Tank.GetBarrelWidth();
		pos.y += tmp.y*Tank.GetBarrelWidth();
		
		// the amount of particles to add at the tanks barrel on launch relative to PPS
		time = 0.0;
		totaltime = 0.0;
		posttime = 0.0;
		posttotaltime = 0.0;
		
		hasfired = false;
	}
	
	public void Update()
	{
		if (hashit) {
			AddTerrainParticles();
			
			return;
		}
		
		if (!hasfired) {
			sfx.play();
			hasfired = true;
			
			time = 0.1f;
			AddParticle();
		}
		
		// apply gravity to the velocity
		vel.y -= GRAVITY * Gdx.graphics.getDeltaTime();
		
		AddParticle();
		
		// update the position
		pos.x += vel.x * Gdx.graphics.getDeltaTime();
		pos.y += vel.y * Gdx.graphics.getDeltaTime();
		
		// wrap the x position
		if (pos.x >= Game.WORLDW) {
			pos.x -= Game.WORLDW;
		} else if (pos.x < 0) {
			pos.x += Game.WORLDW;
		}
		
		if (ter.Contains(pos.x, pos.y)) {
			ter.CutHole((int)pos.x, Game.WORLDH - (int)pos.y, 64);
			
			float theta = GetTheta();
			pos.x += Math.cos(theta)*64;
			pos. y = Game.WORLDH - ter.GetHeight((int)pos.x);
			hashit = true;
		}
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam)
	{
		if (hashit) {
			return;
		}
		
		float theta = GetTheta();
		Batch.draw(tex, Cam.GetRenderX(pos.x + tex.getWidth()/2f), Cam.GetRenderY(pos.y + tex.getHeight()/2f), 
				0, 0, tex.getWidth(), tex.getHeight(),
				1f, 1f, theta, 0, 0, tex.getWidth(), tex.getHeight(), false, false);
	}
	
	public boolean IsCompleted()
	{
		return hashit && (posttotaltime > DUSTTIME);
	}
	
	public boolean HasHit()
	{
		return hashit;
	}
	
	private float GetTheta()
	{
		
		// the angle to draw the missile at
		float theta = (float)Math.toDegrees( Math.atan( vel.y/vel.x ) );
		if (vel.x < 0) {
			theta += 180f;
		}
		
		return theta;
	}
	
	private void AddParticle()
	{
		totaltime += Gdx.graphics.getDeltaTime();
		time += Gdx.graphics.getDeltaTime();
		
		int addcount = (int)(PPS*time);
		if (addcount == 0) {
			return;
		}
		
		for (int i=0; i<addcount; i++) {
			float radius = (float)Math.random()*24 + 12;
			radius *= GetRadiusMod();
			Vector2 v = new Vector2((float)Math.random()*16, (float)Math.random()*16);
			
			float xpos = pos.x + vel.x * (i/(float)addcount) * (float)time;
			float ypos = pos.y + vel.y * (i/(float)addcount) * (float)time;
			
			particle.AddParticle(radius, new Vector2(xpos, ypos), v);
		}
		
		time = 0.0;
	}
	
	private void AddTerrainParticles()
	{
		double prevtot = posttotaltime;
		posttotaltime += Gdx.graphics.getDeltaTime();
		posttime += Gdx.graphics.getDeltaTime();
		
		if (posttotaltime > DUSTTIME) {
			return;
		}
		
		int addcount = (int)(PPS*posttime*2);
		if (addcount == 0) {
			return;
		}
		
		float x0 = Game.WORLDH - ter.GetHeight((int)pos.x - 8);
		float x1 = Game.WORLDH - ter.GetHeight((int)pos.x + 8);
		
		Vector2 v0 = new Vector2(-8, x0-pos.y);
		Vector2 v1 = new Vector2(8, x1-pos.y);
		v0 = v0.nor();
		v1 = v1.nor();
		
		float theta = (float)Math.acos( Vector2.dot(v0.x, v0.y, v1.x, v1.y) );
		theta = -(float)Math.toDegrees(theta);
		
		for (int i=0; i<addcount; i++) {
			double time = prevtot + (float)(posttime) * (i/(float)addcount);
			float radius = (float)Math.random()*24 + 12;
			radius *= GetPostRadiusMod(time);
			
			Vector2 v = new Vector2(v0);
			v = v.nor();
			
			v.x *= (DUSTSPEED * GetPostRadiusMod(time));
			v.y *= (DUSTSPEED * GetPostRadiusMod(time));
			v.rotate((int)(Math.random()*theta));
		
			Vector2 p = new Vector2(pos);
			p.x += v.x * (float)(posttime) * (i/(float)addcount);
			p.y += v.y * (float)(posttime) * (i/(float)addcount);
			
			particle.AddParticle(radius, p, v, DUSTDECAY);
		}
		
		posttime = 0.0;
	}
	
	private float GetRadiusMod()
	{
		if (totaltime > DECAY) {
			return 0f;
		} else {
			return (DECAY - (float)totaltime)/DECAY;
		}
	}
	
	private float GetPostRadiusMod(double TotalTime)
	{
		if (TotalTime > DUSTTIME) {
			return 0f;
		} else {
			return  (DUSTTIME-(float)TotalTime)/DUSTTIME;
		}
	}
}
