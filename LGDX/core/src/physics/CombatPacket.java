package physics;

import particles.Particles;
import terrain.Terrain;
import arsenal.Armament;
import arsenal.Armor;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;

import entity.Unit;

public class CombatPacket 
{
	public static Sound sfx;
	public static Sound reload;
	public static final Color BULLETCOL = new Color(128/255f, 128/255f, 128/255f, 1f);
	public static final int BULLETDIMMENSIONX = 3;
	public static final int BULLETDIMMENSIONY = 2;
	public static final float HALFWIDTH = BULLETDIMMENSIONX/2f;
	
	private static final int PPS = 400; // particles per second
	private static final float DECAY = 0.4f;
	private static Texture tex;
	
	private final Unit offense;
	private final Unit defense;
	private final Armament arms;
	private final Armor armor;
	private boolean iscompleted;
	private int prevdir;
	
	private Vector2 pos;
	private Vector2 target;
	private Vector2 speed;
	
	private Terrain ter;
	private Particles particle;
	private Rectangle targetBBox;
	
	private double delayclock;
	private double delay;
	private double offset;
	
	private double time;
	private double totaltime;
	
	private boolean hasfired;
	
	public static void Init()
	{
		if (tex == null)
		{
			Pixmap tmp = new Pixmap(BULLETDIMMENSIONX, BULLETDIMMENSIONY, Pixmap.Format.RGB888);
			tmp.setColor(BULLETCOL);
			tmp.fill();
			tex = new Texture(tmp);
			tmp.dispose();
		}
		
		if (sfx == null) {
			sfx = Gdx.audio.newSound(Gdx.files.internal("aud/sfx/rifle0.wav"));
		}
		
		if (reload == null) {
			reload = Gdx.audio.newSound(Gdx.files.internal("aud/sfx/reload.wav"));
		}
	}
	
	public static void Release()
	{
		if (tex != null)
			tex.dispose();
		
		if (sfx != null)
			sfx.dispose();
		
		if (reload != null)
			reload.dispose();
	}
	
	public CombatPacket(Terrain Ter, Particles Particles, Unit Offense, Unit Defense, 
			Armament Arms, Armor Armor, float Delay, float Offset)
	{
		delay = Delay;
		offset = Offset;
		ter = Ter;
		particle = Particles;
		offense = Offense;
		defense = Defense;
		arms = Arms;
		armor = Armor;
		iscompleted = false;
		prevdir = 0;
		
		// set the initial position and the target position
		pos = new Vector2(offense.GetPos().x + offense.GetWidth()/2f, 
				offense.GetPos().y + offense.GetHeight()/2f);
		target = new Vector2(defense.GetPos().x, defense.GetPos().y + defense.GetHeight()/2f);
		
		// calculate the speed in each direction
		speed = new Vector2(target);
		int direction = GetMoveDirection();
		if (direction == -1)
			speed.x += defense.GetWidth();
		
		speed.x -= pos.x;
		speed.y -= pos.y;
		speed.nor();
		speed.x = Math.abs(speed.x * arms.GetSpeed());
		speed.y *= arms.GetSpeed();
		
		// set the target bounding box
		targetBBox = new Rectangle(target.x, target.y, defense.GetWidth(), defense.GetHeight());
		
		// the amount of particles to add at the tanks barrel on launch relative to PPS
		time = 0.0;
		totaltime = 0.0;
		AddParticle();
		
		// determines whether or not play the sound effect
		hasfired = false;
	}
	
	public void Update()
	{
		if (iscompleted || !CheckDelay()) {
			return;
		}
		
		if (!hasfired) {
			float pan = 2f*(float)(offset/0.74f)-1f;
			sfx.play((float)(Math.random()*0.35f)+0.05f, 1f, pan);
			hasfired = true;
		}
		
		// check if this unit has reached his position
		if (targetBBox.contains(pos.x + HALFWIDTH, 
				pos.y + HALFWIDTH) || ter.Contains(pos.x, pos.y)) {
			SetCompleted();
			return;
		}
		
		AddParticle();
			
		int direction = GetMoveDirection();
		if (direction == 0 || direction == -prevdir) {
			SetCompleted();
			return;
		}
		
		pos.x += (direction * speed.x * Gdx.graphics.getDeltaTime());
		pos.y += (speed.y * Gdx.graphics.getDeltaTime());
		
		if (pos.x < 0) {
			pos.x += Game.WORLDW;
		} else if (pos.x > Game.WORLDW) {
			pos.x -= Game.WORLDW;
		}
		
		prevdir = direction;
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam)
	{
		if (iscompleted || !CheckDelay()) {
			return;
		}
		
		Batch.draw(tex, Cam.GetRenderX(pos.x), Cam.GetRenderY(pos.y));
	}
	
	public Unit GetOffense()
	{
		return offense;
	}
	
	public Unit GetDefense()
	{
		return defense;
	}
	
	public Vector2 GetPosition()
	{
		return pos;
	}
	
	public Armament GetArmament()
	{
		return arms;
	}
	
	public void SetCompleted()
	{
		// avoid repeat offenders
		if (iscompleted) {
			return;
		}
		
		iscompleted = true;
		ProcHit();
	}
	
	public boolean IsCompleted()
	{
		return iscompleted;
	}
	
	private void ProcHit()
	{
		double doeshit = Math.random();
		if (doeshit > arms.GetAccuracy()) {
			// return early, the attack missed
			return;
		}
		
		float dmg = Math.max(arms.GetStrength() - armor.GetStrength(), 0);
		armor.Damage(arms.GetStrength());
		
		defense.Damage(dmg);
	}
	
	private int GetMoveDirection()
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
	
	private void AddParticle()
	{
		totaltime += Gdx.graphics.getDeltaTime();
		time += Gdx.graphics.getDeltaTime();
		
		int addcount = (int)(PPS*time);
		if (addcount == 0) {
			return;
		} else {
			time = 0.0f;
		} 
		
		int direction = GetMoveDirection();
		for (int i=0; i<addcount; i++) {
			float radius = (float)Math.random()*8+ 2;
			radius *= GetRadiusMod();
			Vector2 vel = new Vector2((float)Math.random()*4, (float)Math.random()*4);
			
			float xpos = pos.x + direction * speed.x * (i/(float)addcount) * Gdx.graphics.getDeltaTime();
			float ypos = pos.y + speed.y * (i/(float)addcount) * Gdx.graphics.getDeltaTime();
			
			particle.AddParticle(radius, new Vector2(xpos, ypos), vel);
		}
	}
	
	private boolean CheckDelay()
	{
		if (delay + offset < delayclock) {
			return true;
		}
		
		delayclock += Gdx.graphics.getDeltaTime();
		return false;
	}
	
	private float GetRadiusMod()
	{
		if (totaltime > DECAY) {
			return 0f;
		} else {
			return (DECAY - (float)totaltime)/DECAY;
		}
	}
}
