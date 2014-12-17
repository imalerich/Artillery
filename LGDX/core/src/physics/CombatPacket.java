package physics;

import particles.Particles;
import terrain.Terrain;
import arsenal.Armament;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;

import entity.Unit;
import entity.UserArmy;

public class CombatPacket 
{
	public static Sound sfx;
	public static Sound reload;
	public static final int MINFOXDIST = 128;
	public static final Color BULLETCOL = new Color(55/255f, 55/255f, 55/255f, 1f);
	public static final int BULLETDIMMENSIONX = 4;
	public static final int BULLETDIMMENSIONY = 4;
	public static final int FLASHSIZE = 18;
	public static final int HALFWIDTH = BULLETDIMMENSIONX/2;
	
	private static final int PPS = 400; // particles per second
	private static final float DECAY = 0.4f;
	private static Texture tex;
	private static Texture flash;
	
	private final Unit offense;
	private final Unit defense;
	private final Armament arms;
	private boolean iscompleted;
	private int prevdir;
	
	private Vector2 source;
	private Vector2 pos;
	private Vector2 target;
	private Vector2 vel;
	
	private Terrain ter;
	private Particles particle;
	private Rectangle targetBBox;
	
	private double delayclock;
	private double delay;
	private double offset;
	
	private double time;
	private double totaltime;
	private boolean targetInFox;
	
	private float distancetraveled;
	private boolean hasfired;
	
	public static void init()
	{
		if (tex == null) {
			Pixmap tmp = new Pixmap(BULLETDIMMENSIONX, BULLETDIMMENSIONY, Pixmap.Format.RGBA8888);
			tmp.setColor(Color.CLEAR);
			tmp.fill();
			tmp.setColor(Color.WHITE);
			tmp.fillCircle(HALFWIDTH, HALFWIDTH, HALFWIDTH);
			
			tex = new Texture(tmp);
			tmp.dispose();
		}
		
		if (flash == null) {
			Pixmap tmp = new Pixmap(FLASHSIZE, FLASHSIZE, Pixmap.Format.RGBA8888);
			tmp.setColor(Color.CLEAR);
			tmp.fill();
			tmp.setColor(Color.WHITE);
			tmp.fillCircle(FLASHSIZE/2, FLASHSIZE/2, FLASHSIZE/2);
			
			flash = new Texture(tmp);
			tmp.dispose();
		}
		
		if (sfx == null) {
			sfx = Gdx.audio.newSound(Gdx.files.internal("aud/sfx/rifle0.wav"));
		}
		
		if (reload == null) {
			reload = Gdx.audio.newSound(Gdx.files.internal("aud/sfx/reload.wav"));
		}
	}
	
	public static void release()
	{
		if (tex != null)
			tex.dispose();
		
		if (flash != null)
			flash.dispose();
		
		if (sfx != null)
			sfx.dispose();
		
		if (reload != null)
			reload.dispose();
	}
	
	public CombatPacket(Terrain Ter, Particles Particles, Unit Offense, Unit Defense, 
			Armament Arms, float Delay, float Offset, boolean TargetInFox)
	{
		delay = Delay;
		offset = Offset;
		ter = Ter;
		particle = Particles;
		offense = Offense;
		defense = Defense;
		arms = Arms;
		iscompleted = false;
		prevdir = 0;
		targetInFox = TargetInFox;
		
		// set the initial position and the target position
		pos = new Vector2(offense.getPos().x + offense.getWidth()/2f, 
				offense.getPos().y + offense.getHeight()/2f);
		target = new Vector2(defense.getPos().x, defense.getPos().y + defense.getHeight()/2f);
		
		// calculate the speed in each direction
		vel = new Vector2(target);
		int direction = getMoveDirection();
		if (direction == -1)
			vel.x += defense.getWidth();
		
		vel.x -= pos.x;
		vel.y -= pos.y;
		vel.nor();
		vel.x = Math.abs(vel.x * arms.getSpeed());
		vel.y *= arms.getSpeed();
		
		// set the source position
		Vector2 tmp = new Vector2(vel);
		tmp.x *= getMoveDirection();
		tmp.nor();
		source = new Vector2(pos);
		source.x += tmp.x * Offense.getWidth()/2f;
		source.y += tmp.y * Offense.getHeight()/2f;
		
		// set the target bounding box
		targetBBox = new Rectangle(target.x, target.y, defense.getWidth(), defense.getHeight());
		
		// the amount of particles to add at the tanks barrel on launch relative to PPS
		time = 0.0;
		totaltime = 0.0;
		addParticle();
		
		// determines whether or not play the sound effect
		hasfired = false;
		distancetraveled = 0f;
	}
	
	public void update(Camera Cam)
	{
		if (iscompleted || !checkDelay()) {
			// do not fire again if the target is dead
			if (!defense.isAlive()) {
				iscompleted = true;
			}
			
			return;
		}
		
		if (!hasfired) {
			float vol = (float)(Math.random()*0.35f + 0.05f);
			offense.getSquad().getArmy().getWorld().getAudio().playSound(sfx, source.x, vol);
			
			hasfired = true;
			addKick(Cam);
		}
		
		// check if this unit has reached his position
		if (targetBBox.contains(pos.x + HALFWIDTH, 
				pos.y + HALFWIDTH) || ter.contains(pos.x, pos.y)) {
			setCompleted();
			return;
		}
		
		addParticle();
			
		int direction = getMoveDirection();
		if (direction == 0 || direction == -prevdir) {
			setCompleted();
			return;
		}
		
		pos.x += (direction * vel.x * Gdx.graphics.getDeltaTime());
		pos.y += (vel.y * Gdx.graphics.getDeltaTime());
		distancetraveled += ( vel.len() * Gdx.graphics.getDeltaTime() );
		
		if (pos.x < 0) {
			pos.x += Game.WORLDW;
		} else if (pos.x > Game.WORLDW) {
			pos.x -= Game.WORLDW;
		}
		
		prevdir = direction;
	}
	
	protected void addKick(Camera Cam)
	{
		// get the distance from the point of fire to the center of the screen
		if (!(offense.getSquad().getArmy() instanceof UserArmy))
			return;
		
		float dist = Vector2.dst(pos.x, pos.y, Cam.getPos().x + Game.SCREENW/2f, Cam.getPos().y + Game.SCREENH/2f);
		Vector2 tmp = new Vector2(vel);
		tmp.nor();

		float mag = 4;
		if (dist > Game.SCREENW)
			mag = 0f;
		else if (dist > 0f) {
			mag *= (Game.SCREENW-dist)/(Game.SCREENW);
		}

		Cam.addKick(-getMoveDirection()*tmp.x*mag, -tmp.y*mag);
	}
	
	public void draw(SpriteBatch Batch, Camera Cam)
	{
		if (iscompleted || !checkDelay()) {
			return;
		}
		
		if (totaltime < 0.3f) {
			float scale = (float)((Math.log(totaltime/0.3f) + Math.E)/Math.E);
			scale = Math.max(Math.min(1f, scale), 0f);
			
			float off = (FLASHSIZE - (FLASHSIZE*scale))/2f;
			float xpos = Cam.getRenderX(source.x + off);
			float ypos = Cam.getRenderY(source.y + off);
			
			Batch.draw(flash, xpos, ypos, FLASHSIZE*scale, FLASHSIZE*scale);
		}
		
		Batch.setColor(BULLETCOL);
		Batch.draw(tex, Cam.getRenderX(pos.x), Cam.getRenderY(pos.y));
		Batch.setColor(Color.WHITE);
	}
	
	public Unit getOffense()
	{
		return offense;
	}
	
	public Unit getDefense()
	{
		return defense;
	}
	
	public Vector2 getPosition()
	{
		return pos;
	}
	
	public Armament getArmament()
	{
		return arms;
	}
	
	public void setCompleted()
	{
		// avoid repeat offenders
		if (iscompleted) {
			return;
		}
		
		iscompleted = true;
		procHit();
	}
	
	public boolean isCompleted()
	{
		return iscompleted;
	}
	
	private void procHit()
	{
		double doeshit = Math.random();
		if (doeshit > arms.getAccuracy()) {
			// return early, the attack missed
			return;
		}
		
		float dmg = Math.max(arms.getStrength() - defense.getArmor().getStrength(), 0);
		if (distancetraveled > MINFOXDIST && targetInFox)
			dmg = 0f;
		
		defense.getArmor().damage(arms.getStrength());
		defense.damage(dmg);
	}
	
	private int getMoveDirection()
	{
		int width = BULLETDIMMENSIONX;


		// check the distance to the target in each direction
		float rdist = (Game.WORLDW-(pos.x+width)) + target.x;
		if (target.x > pos.x)
			rdist = target.x -(pos.x);

		float ldist = pos.x + (Game.WORLDW - target.x);
		if (target.x < pos.x)
			ldist = (pos.x - target.x);
		
		if (rdist < ldist) {
			return 1;
		} else if (ldist < rdist) {
			return -1;
		} else {
			return 0;
		}
	}
	
	private void addParticle()
	{
		totaltime += Gdx.graphics.getDeltaTime();
		time += Gdx.graphics.getDeltaTime();
		
		int addcount = (int)(PPS*time);
		if (addcount == 0) {
			return;
		} else {
			time = 0.0f;
		} 
		
		int direction = getMoveDirection();
		for (int i=0; i<addcount; i++) {
			float radius = (float)Math.random()*8+ 2;
			radius *= getRadiusMod();
			Vector2 vel = new Vector2((float)Math.random()*4, (float)Math.random()*4);
			
			float xpos = pos.x + direction * vel.x * (i/(float)addcount) * Gdx.graphics.getDeltaTime();
			float ypos = pos.y + vel.y * (i/(float)addcount) * Gdx.graphics.getDeltaTime();
			
			particle.addParticle(radius, new Vector2(xpos, ypos), vel);
		}
	}
	
	private boolean checkDelay()
	{
		if (delay + offset < delayclock) {
			return true;
		}
		
		delayclock += Gdx.graphics.getDeltaTime();
		return false;
	}
	
	private float getRadiusMod()
	{
		if (totaltime > DECAY) {
			return 0f;
		} else {
			return (DECAY - (float)totaltime)/DECAY;
		}
	}
}
