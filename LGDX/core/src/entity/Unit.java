package entity;

import java.util.Vector;

import network.Response;
import particles.Particles;
import physics.NullTank;
import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;

public class Unit 
{
	protected Terrain ter;
	protected Vector2 pos;
	protected boolean forward;
	protected boolean moving;
	protected int speed;
	protected float health;
	protected float maxhealth;
	protected boolean takedirectdamage = true;
	
	protected int width = 0;
	protected int height = 0;
	
	protected Vector2 barrelsrc = new Vector2();
	protected boolean isFiring = false;
	protected int mugshotIndex = 0;
	protected int id;
	
	private Squad squad;
	
	public void setUnitData(int Speed, float Health, int ViewRadius)
	{
		speed = Speed;
		health = Health;
		maxhealth = Health;
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
			return;
		}
		
		health -= Dmg;
		
		// inform remote squads of the damage
		Response r = new Response();
		r.source = getSquad().getArmy().getConnection();
		r.request = "UNITHEALTH";
		r.i0 = getSquad().getID();
		r.i1 = getID();
		r.f0 = health;
		r.f1 = maxhealth;
		
		getSquad().getArmy().getNetwork().getClient().sendTCP(r);
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
		
		getSquad().getArmy().getNetwork().getClient().sendTCP(r);
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
	
	public void moveRight()
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
	
	public void moveLeft()
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
	
	public void setAsDeceased(Vector<NullTank> Deceased, Particles Part)
	{
		ter.addDeceasedTroop((int)pos.x, forward);
	}
	
	public void setHeight()
	{
		// set the new height
		int nxtpos = (int)pos.x + width/2;
		if (nxtpos >= Game.WORLDW) nxtpos -= Game.WORLDW;
		if (nxtpos < 0) nxtpos += Game.WORLDW;
		
		pos.y = Game.WORLDH - ter.getHeight(nxtpos) - 3;
	}
	
	public void drawTargetAngle(SpriteBatch Batch, Camera Cam)
	{
		// override in implementation classes
	}
	
	public void draw(SpriteBatch Batch, Camera Cam, boolean Highlight, boolean Target)
	{
		// override in implementation classes
	}
}
