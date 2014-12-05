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
	
	protected boolean isFiring = false;
	protected int mugshotIndex = 0;
	protected int id;
	
	private Squad squad;
	
	public void SetUnitData(int Speed, float Health, int ViewRadius)
	{
		speed = Speed;
		health = Health;
		maxhealth = Health;
	}
	
	public void SetSquad(Squad S)
	{
		squad = S;
	}
	
	public Squad GetSquad()
	{
		return squad;
	}
	
	public void Release()
	{
		// override in implementation classes
	}
	
	public void SetID(int ID)
	{
		id = ID;
	}
	
	public int GetID()
	{
		return id;
	}
	
	public void SetDirectDamage(boolean B)
	{
		takedirectdamage = B;
	}
	
	public boolean TakesDirectDamage()
	{
		return takedirectdamage;
	}
	
	public void SetFiring(boolean IsFiring)
	{
		isFiring = IsFiring;
	}
	
	public Rectangle GetBBox()
	{
		return new Rectangle(pos.x, pos.y, width, height);
	}
	
	public float GetAngle()
	{
		return 0.0f;
	}
	
	public float GetBarrelAbsoluteAngle()
	{
		return 0.0f;
	}
	
	public void SetBarrelAngle(float Angle)
	{
		//
	}
	
	public int GetMugShotIndex()
	{
		return mugshotIndex;
	}
	
	public void SetMugShotIndex(int Index)
	{
		mugshotIndex = Index;
	}
	
	public boolean IsAlive()
	{
		return health > 0;
	}
	
	public float GetHealth()
	{
		return health;
	}
	
	public void SetHealth(float Health)
	{
		health = Health;
	}
	
	public void SetMaxHealth(float MaxHealth)
	{
		maxhealth = MaxHealth;
	}
	
	public void Damage(float Dmg)
	{
		if (!takedirectdamage) {
			return;
		}
		
		health -= Dmg;
		
		// inform remote squads of the damage
		Response r = new Response();
		r.source = GetSquad().GetArmy().GetConnection();
		r.request = "UNITHEALTH";
		r.i0 = GetSquad().GetID();
		r.i1 = GetID();
		r.f0 = health;
		r.f1 = maxhealth;
		
		GetSquad().GetArmy().GetNetwork().GetClient().sendTCP(r);
	}
	
	public void SetMaxHealth()
	{
		health = maxhealth;
	}
	
	public void Heal(float Amt)
	{
		if (!takedirectdamage) {
			return;
		}
		
		health += Amt;
		
		// inform remote squads of the damage
		Response r = new Response();
		r.source = GetSquad().GetArmy().GetConnection();
		r.request = "UNITHEALTH";
		r.i0 = GetSquad().GetID();
		r.i1 = GetID();
		r.f0 = health;
		
		GetSquad().GetArmy().GetNetwork().GetClient().sendTCP(r);
	}
	
	public Vector2 GetPos()
	{
		return pos;
	}
	
	public void SetPos(Vector2 Pos)
	{
		pos = Pos;
	}
	
	public void SetForward(boolean IsForward)
	{
		forward = IsForward;
	}
	
	public boolean IsForward()
	{
		return forward;
	}
	
	public int GetWidth()
	{
		return width;
	}
	
	public int GetHeight()
	{
		return height;
	}
	
	public int GetSpeed()
	{
		return speed;
	}
	
	public Terrain GetTerrainReference()
	{
		return ter;
	}
	
	public void SetTerrainReference(Terrain Ter)
	{
		ter = Ter;
	}
	
	public void MoveRight()
	{
		int midpoint = width/2;
		int frontpoint = midpoint + width/4;
		
		// sample the direction traveled
		float tanspeed = Gdx.graphics.getDeltaTime()*speed;
		int nxtpos = (int)pos.x+frontpoint;
		if (nxtpos >= Game.WORLDW) nxtpos -= Game.WORLDW;
		if (nxtpos < 0) nxtpos += Game.WORLDW;
		int nxth = Game.WORLDH - ter.GetHeight(nxtpos) - 3;
		
		float theta = -(float)Math.atan( (nxth-pos.y)/(width/4.0f) );
		float xspeed = (float)Math.cos(theta)*tanspeed;
		
		pos.x += xspeed; 
		if (pos.x >= Game.WORLDW) pos.x -= Game.WORLDW;
		if (pos.x < 0) pos.x += Game.WORLDW;
		forward = true;
		moving = true;
		
		SetHeight();
	}
	
	public void MoveLeft()
	{
		int midpoint = width/2;
		int backpoint = midpoint - width/4;
		
		// sample the direction traveled
		float tanspeed = Gdx.graphics.getDeltaTime()*speed;
		int nxtpos = (int)pos.x+backpoint;
		if (nxtpos < 0) nxtpos += Game.WORLDW;
		if (nxtpos >= Game.WORLDW) nxtpos -= Game.WORLDW;
		int nxth = Game.WORLDH - ter.GetHeight(nxtpos) - 3;
		
		float theta = -(float)Math.atan( (pos.y-nxth)/(width/4.0f) );
		float xspeed = (float)Math.cos(theta)*tanspeed;
		
		pos.x -= xspeed;
		if (pos.x < 0) pos.x += Game.WORLDW;
		if (pos.x >= Game.WORLDW) pos.x -= Game.WORLDW;
		forward = false;
		moving = true;
		
		SetHeight();
	}
	
	public void SetAsDeceased(Vector<NullTank> Deceased, Particles Part)
	{
		ter.AddDeceasedTroop((int)pos.x, forward);
	}
	
	public void SetHeight()
	{
		// set the new height
		int nxtpos = (int)pos.x + width/2;
		if (nxtpos >= Game.WORLDW) nxtpos -= Game.WORLDW;
		if (nxtpos < 0) nxtpos += Game.WORLDW;
		
		pos.y = Game.WORLDH - ter.GetHeight(nxtpos) - 3;
	}
	
	public void DrawTargetAngle(SpriteBatch Batch, Camera Cam)
	{
		// override in implementation classes
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam, boolean Highlight, boolean Target)
	{
		// override in implementation classes
	}
}
