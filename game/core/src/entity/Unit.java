package entity;

import java.util.Vector;

import network.Response;
import particles.Particles;
import physics.NullTank;
import terrain.Terrain;
import arsenal.Armor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;

public abstract class Unit 
{
	private static final int PPS = 1200;
	
	protected Terrain ter;
	protected Vector2 pos;
	protected boolean forward;
	protected boolean moving;
	protected int speed;
	protected float health;
	protected float maxhealth;
	protected boolean takedirectdamage = true;
	protected boolean useFullWidthForY = false;
	
	protected int width = 0;
	protected int height = 0;
	
	protected int onfire = 0;
	protected double firetimer = 0f;
	
	protected Vector2 barrelsrc = new Vector2();
	protected boolean isFiring = false;
	protected int mugshotIndex = 0;
	protected int id;
	protected float animtime = 0f;
	protected Armor armor;
	
	// the requisition bonus given when dead
	private int reqbonus = 0;
	
	private Squad squad;
	
	public void setUnitData(int Speed, float Health, int ViewRadius)
	{
		speed = Speed;
		health = Health;
		maxhealth = Health;
	}
	
	public void setReqBonus(int Bonus)
	{
		reqbonus = Bonus;
	}
	
	public int getReqBonus()
	{
		return reqbonus;
	}
	
	public void setArmor(Armor A)
	{
		armor = new Armor(A);
	}
	
	public Armor getArmor()
	{
		return armor;
	}
	
	public void setSquad(Squad S)
	{
		squad = S;
	}
	
	public Squad getSquad()
	{
		return squad;
	}
	
	public void release()
	{
		// override in implementation classes
	}
	
	public void setID(int ID)
	{
		id = ID;
	}
	
	public int getID()
	{
		return id;
	}
	
	public int isOnFire()
	{
		return onfire;
	}
	
	public void setOnFire(int F)
	{
		onfire = Math.max(F, 0);
	}
	
	public void setDirectDamage(boolean B)
	{
		takedirectdamage = B;
	}
	
	public boolean takesDirectDamage()
	{
		return takedirectdamage;
	}
	
	public void setFiring(boolean IsFiring)
	{
		isFiring = IsFiring;
	}
	
	public Rectangle getBBox()
	{
		return new Rectangle(pos.x, pos.y, width, height);
	}
	
	public float getAngle()
	{
		return 0.0f;
	}
	
	public float getBarrelAbsoluteAngle()
	{
		return 0.0f;
	}
	
	public void setBarrelAngle(float Angle)
	{
		//
	}
	
	public int getMugShotIndex()
	{
		return mugshotIndex;
	}
	
	public void setMugShotIndex(int Index)
	{
		mugshotIndex = Index;
	}
	
	public Vector2 getBarrelSrc()
	{
		return barrelsrc;
	}
	
	public void setBarrelSrc(Vector2 BarrelSrc)
	{
		barrelsrc = new Vector2(BarrelSrc);
	}
	
	public boolean isAlive()
	{
		return health > 0;
	}
	
	public float getHealthPercentage()
	{
		return health/maxhealth;
	}
	
	public float getArmorPercentage()
	{
		return armor.getHealth()/armor.getMaxHealth();
	}
	
	public float getHealth()
	{
		return health;
	}
	
	public void setHealth(float Health)
	{
		health = Health;
	}
	
	public void setMaxHealth(float MaxHealth)
	{
		maxhealth = MaxHealth;
	}
	
	public void damage(float Dmg)
	{
		if (!takedirectdamage) {
			System.out.println("The target unit does not take direct damage.\n");
			return;
		}
		
		System.out.print("Health was " + health + ". . . ");
		health -= Dmg;
		System.out.println("but now is " + health + "!\n");
		
		// inform remote squads of the damage
		Response r = new Response();
		r.source = getSquad().getArmy().getConnection();
		r.request = "UNITHEALTH";
		r.i0 = getSquad().getID();
		r.i1 = getID();
		r.f0 = health;
		r.f1 = maxhealth;
		
		if (Game.NETWORKED)
			getSquad().getArmy().getNetwork().getUserClient().sendTCP(r);
	}
	
	public void setMaxHealth()
	{
		health = maxhealth;
	}
	
	public void heal(float Amt)
	{
		if (!takedirectdamage) {
			return;
		}
		
		health += Amt;
		
		// inform remote squads of the damage
		Response r = new Response();
		r.source = getSquad().getArmy().getConnection();
		r.request = "UNITHEALTH";
		r.i0 = getSquad().getID();
		r.i1 = getID();
		r.f0 = health;
		
		if (Game.NETWORKED)
			getSquad().getArmy().getNetwork().getUserClient().sendTCP(r);
	}
	
	public Vector2 getPos()
	{
		return pos;
	}
	
	public void getPos(Vector2 Pos)
	{
		pos = Pos;
	}
	
	public void setForward(boolean IsForward)
	{
		forward = IsForward;
	}
	
	public boolean isForward()
	{
		return forward;
	}
	
	public float getForward()
	{
		if (forward)
			return 1f;
		else
			return -1f;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public int getSpeed()
	{
		return speed;
	}
	
	public Terrain getTerrainReference()
	{
		return ter;
	}
	
	public void setTerrainReference(Terrain Ter)
	{
		ter = Ter;
	}
	
	protected Vector2 rotateCoord(Vector2 Coord, float Theta)
	{
		float x = Coord.x;
		float y = Coord.y;
		
		float cos = (float)Math.cos( Theta );
		float sin = (float)Math.sin( Theta );
		
		float rx = x*cos - y*sin;
		float ry = x*sin + y*cos;
		
		return new Vector2(rx, ry);
	}
	
	public void moveRight(Camera Cam)
	{
		int midpoint = width/2;
		int frontpoint = midpoint + width/4;
		
		// sample the direction traveled
		float tanspeed = Gdx.graphics.getDeltaTime()*speed;
		int nxtpos = (int)pos.x+frontpoint;
		if (nxtpos >= Game.WORLDW) nxtpos -= Game.WORLDW;
		if (nxtpos < 0) nxtpos += Game.WORLDW;
		int nxth = Game.WORLDH - ter.getHeight(nxtpos) - 3;
		
		float theta = -(float)Math.atan( (nxth-pos.y)/(width/4.0f) );
		float xspeed = (float)Math.cos(theta)*tanspeed;
		
		pos.x += xspeed; 
		if (pos.x >= Game.WORLDW) pos.x -= Game.WORLDW;
		if (pos.x < 0) pos.x += Game.WORLDW;
		forward = true;
		moving = true;
		
		setHeight();
	}
	
	public void moveLeft(Camera Cam)
	{
		int midpoint = width/2;
		int backpoint = midpoint - width/4;
		
		// sample the direction traveled
		float tanspeed = Gdx.graphics.getDeltaTime()*speed;
		int nxtpos = (int)pos.x+backpoint;
		if (nxtpos < 0) nxtpos += Game.WORLDW;
		if (nxtpos >= Game.WORLDW) nxtpos -= Game.WORLDW;
		int nxth = Game.WORLDH - ter.getHeight(nxtpos) - 3;
		
		float theta = -(float)Math.atan( (pos.y-nxth)/(width/4.0f) );
		float xspeed = (float)Math.cos(theta)*tanspeed;
		
		pos.x -= xspeed;
		if (pos.x < 0) pos.x += Game.WORLDW;
		if (pos.x >= Game.WORLDW) pos.x -= Game.WORLDW;
		forward = false;
		moving = true;
		
		setHeight();
	}
	
	public void updateEmbers()
	{
		if (onfire > 0) {
			firetimer += Gdx.graphics.getDeltaTime();

			int addcount = (int)(PPS*firetimer);
			if (addcount == 0) {
				return;
			}

			for (int i=0; i<addcount; i++) {
				firetimer = 0f;
				float scale = (float)Math.random()/2f + 0.2f;
				float xmod = (float)Math.random();
				float xpos = pos.x + xmod*10f + 8;
				if (!forward)
					xpos = pos.x + getWidth() - xmod*10f - 8f;
				float ypos = pos.y + 16f * (float)(-(Math.pow(2f*(xmod-0.5f), 2f)) + 1f);

				getSquad().getArmy().getWorld().getParticles().addEmber(scale, new Vector2(xpos, ypos));
			}
			
			firetimer = 0f;
		}
	}
	
	public void setAsDeceased(Vector<NullTank> Deceased, Particles Part)
	{
		ter.addDeceasedTroop((int)pos.x, forward);
	}
	
	public void setHeight()
	{
		// set the new height
		if (useFullWidthForY)
			pos.y = Game.WORLDH - ter.getMaxHeight((int)pos.x, (int)pos.x + width) - 1;
		else
			pos.y = Game.WORLDH - ter.getHeight((int)(pos.x + width/2)) - 3;
	}
	
	public void drawTargetAngle(SpriteBatch Batch, Camera Cam)
	{
		// override in implementation classes
	}
	
	public abstract void draw(SpriteBatch Batch, Camera Cam, boolean Highlight, boolean Target);
}
