package entity;

import java.util.Iterator;
import java.util.Vector;

import network.Response;
import particles.Particles;
import physics.Blast;
import physics.GameWorld;
import physics.NullTank;
import terrain.FogOfWar;
import terrain.Terrain;
import arsenal.Armament;
import arsenal.Armor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.AnimTex;
import com.mygdx.game.Camera;
import com.mygdx.game.Cursor;
import com.mygdx.game.Game;

public class Squad 
{
	public static Texture pointer;
	public static AnimTex target;
	private static TextureRegion[] mugshots;
	private static final int MUGSHOTCOUNT = 4;
	private static final int SPACING = 2;
	
	private static int MAXHEIGHT = 12;
	private int squadspacing = 32;
	private int maxmovedist = 0;
	private int id = -1;
	
	// the armor and armament that is used by this squadbn
	private Armament arms;
	private Armor armor;
	
	private Vector<Unit> units;
	private Rectangle bbox;
	private float minx, maxx;
	private Terrain ter;
	
	private Squad targetsquad;
	private int targetpos;
	private boolean ismoving;
	private boolean isforward;
	
	private boolean isFiring;
	private boolean isTarget;
	private boolean direction;
	private double pointerheight;
	private Vector2 barrelsrc;
	private int addid = 0;
	private boolean takesdirectdamage = true;
	
	private boolean addfox;
	private Vector2 foxpos;
	
	private Army army;
	
	public static void Init()
	{
		if (pointer == null)
			pointer = new Texture( Gdx.files.internal("img/ui/indicators/pointer.png") );
		
		if (target == null) {
			target = new AnimTex("img/ui/indicators/target.png", 1, 4, 1);
			target.NewAnimation(0, 4, 0, 3, 0.12f);
		}
		
		if (mugshots == null) {
			Texture tmp = new Texture( Gdx.files.internal("img/ui/profile/mugshots.png") );
			mugshots = TextureRegion.split(tmp, 32, 32)[0];
		}
	}
	
	public static void Release()
	{
		if (pointer != null)
			pointer.dispose();
		
		if (target != null)
			target.Release();
	}
	
	public Squad(Terrain Ter, int MoveDist, Army A)
	{
		units = new Vector<Unit>();
		bbox = new Rectangle(0, 0, Float.MAX_VALUE, Float.MAX_VALUE);
		minx = maxx = 0f;
		maxmovedist = MoveDist;
		army = A;
		
		ter = Ter;
		targetsquad = null;
		targetpos = -1;
		ismoving = false;
		
		pointerheight = (int)(Math.random()*MAXHEIGHT);
		direction = true; // up
		isTarget = false;
		isforward = true;
		
		barrelsrc = new Vector2();
	}
	
	public Army GetArmy()
	{
		return army;
	}
	
	public void SetID(int ID)
	{
		id = ID;
	}
	
	public int GetID()
	{
		return id;
	}
	
	public void SetBarrelSrc(Vector2 Pos)
	{
		barrelsrc = Pos;
	}
	
	public Vector2 GetBarrelSrc()
	{
		if (isforward) {
			return new Vector2(barrelsrc);
		} else {
			return new Vector2(bbox.width - barrelsrc.x, barrelsrc.y);
		}
	}
	
	public void SetMaxHealth()
	{
		Iterator<Unit> u = units.iterator();
		while (u.hasNext())
		{
			u.next().SetMaxHealth();
		}
	}
	
	public void AddFoxOnFinishMove(Vector2 Pos)
	{
		addfox = true;
		foxpos = Pos;
	}
	
	public void TakesDirectDamage(boolean B)
	{
		takesdirectdamage = B;
		Iterator<Unit> u = units.iterator();
		while (u.hasNext())
		{
			u.next().SetDirectDamage(takesdirectdamage);
		}
	}
	
	public void CheckAlive(Vector2 Campos, Vector<NullTank> Deceased, Particles Part)
	{
		Iterator<Unit> u = units.iterator();
		while (u.hasNext())
		{
			// remove all dead units
			Unit unit = u.next();
			if (!unit.IsAlive()) {
				unit.SetAsDeceased(Deceased, Part);
				u.remove();
			}
		}
		
		CalcBoundingBox(Campos);
	}
	
	public void ProcBlasts(Blast B)
	{
		Iterator<Unit> u = units.iterator();
		while (u.hasNext())
		{
			// process damage to units done by any blasts
			Unit unit = u.next();
				
			if (Intersector.overlaps(new Circle(B.pos, B.radius), unit.GetBBox())) {
				float dmg = Math.max(B.strength - armor.GetStrength(), 0);
				armor.Damage((int)B.strength);

				unit.Damage(dmg);
			}
		}
	}
	
	public Iterator<Unit> GetUnitIterator()
	{
		return units.iterator();
	}
	
	public int GetMoveDist()
	{
		return maxmovedist;
	}
	
	public boolean IsForward()
	{
		return isforward;
	}
	
	public void SetFiring(boolean IsFiring)
	{
		isFiring = IsFiring;
	}
	
	public boolean IsFiring()
	{
		return isFiring;
	}
	
	public void SetAsTarget()
	{
		isTarget = true;
	}
	
	public Vector<Unit> GetUnits()
	{
		return units;
	}
	
	public Rectangle GetBBox()
	{
		return bbox;
	}
	
	public void SetArmament(Armament Arms)
	{
		arms = new Armament(Arms);
	}
	
	public Armament GetArmament()
	{
		return arms;
	}
	
	public void SetArmor(Armor Set)
	{
		armor = new Armor(Set);
	}
	
	public Armor GetArmor()
	{
		return armor;
	}
	
	public int GetTargetX()
	{
		return targetpos;
	}
	
	public void SetTargetX(int Target)
	{
		targetpos = Target;
		ModTarget();
	}
	
	public Squad GetTargetSquad()
	{
		return targetsquad;
	}
	
	public void SetTargetSquad(Squad Target)
	{
		targetsquad = Target;
		if (targetsquad == null)
			return;
		
		// face the target squad
		int direction = GameWorld.GetDirection(bbox.x, 1, 
				targetsquad.GetBBox().x, 1);
		
		Iterator<Unit> u = units.iterator();
		while (u.hasNext()) {
			u.next().SetForward(direction == 1);
		}
	}
	
	public Rectangle GetBoundingBox()
	{
		return bbox;
	}
	
	public int GetUnitCount()
	{
		return units.size();
	}
	
	public float GetBarrelAngle()
	{
		return arms.GetAngle();
	}
	
	public void SetBarrelAngle(float Angle)
	{
		// all squad members must have the same angle
		Iterator<Unit> u = units.iterator();
		while (u.hasNext()) {
			Unit unit = u.next();
			
			unit.SetBarrelAngle(Angle);
			arms.SetAngle( unit.GetBarrelAbsoluteAngle() );
		}
	}
	
	private void CalcBoundingBox(Vector2 Campos)
	{
		// do not calculate the bounding box if there are no units in this squad
		if (units.size() == 0) {
			return;
		}
		
		// set to max to guarantee override
		Vector2 min = new Vector2(Float.MAX_VALUE, Float.MAX_VALUE);
		Vector2 max = new Vector2(Float.MIN_VALUE, Float.MIN_VALUE);
		minx = Float.MAX_VALUE;
		maxx = Float.MIN_VALUE;
		
		// the first x position determines the orientation of the bounding box relative to the world map
		float firstx = units.get(0).GetPos().x;
		
		Iterator<Unit> i = units.iterator();
		while (i.hasNext())
		{
			// convert he position to screen coordinates
			Unit n = i.next();
			Rectangle r = n.GetBBox();
			
			// used for determing whether or not to draw the target position
			if (n.GetPos().x < minx)
				minx = n.GetPos().x;
			if (n.GetPos().x + n.GetWidth() > maxx)
				maxx = n.GetPos().x + n.GetWidth();
			
			// used for mouse over operations
			if (r.x >= firstx) {
				if (r.x < min.x)
					min.x = r.x;
				if (r.x+r.width > max.x)
					max.x = r.x+r.width;
			} else {
				// units should be in ascending order
				if (r.x + Game.WORLDW < min.x)
					min.x = r.x + Game.WORLDW;
				if (r.x + Game.WORLDW + r.width > max.x)
					max.x = r.x + Game.WORLDW + r.width;
			}
			
			if (r.y < min.y)
				min.y = r.y;
			if (r.y+r.height > max.y)
				max.y = r.y+r.height;
		}
		
		// construct the new bounding box
		bbox = new Rectangle(min.x, min.y, max.x-min.x, max.y-min.y);
		if (bbox.x < 0)
			bbox.x += Game.WORLDW;
		else if (bbox.x > Game.WORLDW)
			bbox.x -= Game.WORLDW;
	}
	
	public boolean IsIntersectingView(Rectangle R)
	{
		// find if any of the squads units view overlaps the target
		Iterator<Unit> u = GetUnitIterator();
		while (u.hasNext()) {
			Unit unit = u.next();
			Circle c = new Circle(unit.GetPos().x + unit.GetWidth()/2, 
					unit.GetPos().y + unit.GetHealth()/2, arms.GetRange());

			if (Intersector.overlaps(c, R)) {
				return true;
			}
		}
		
		return false;
	}
	
	public void SetForward(boolean Forward)
	{
		Iterator<Unit> i = units.iterator();
		while (i.hasNext()) {
			i.next().SetForward(Forward);
		}
	}
	
	public boolean IsMouseOver(Vector2 Campos)
	{
		CalcBoundingBox(Campos);
		return Cursor.IsMouseOver(bbox, Campos);
	}
	
	public boolean IsMoving()
	{
		return ismoving;
	}
	
	public void SetSquadSpacing(int SquadSpacing)
	{
		squadspacing = SquadSpacing;
	}
	
	public int GetSquadSpacing()
	{
		return squadspacing;
	}
	
	public void AddUnit(Unit Add)
	{
		// get the position at which to add this unit
		Vector2 addp =  new Vector2(Add.GetPos());
		
		Add.SetPos(addp);
		Add.SetHeight();
		units.add(Add);
		units.lastElement().SetID(addid);
		units.lastElement().SetDirectDamage(takesdirectdamage);
		units.lastElement().SetSquad(this);
		addid++;
	}
	
	public Unit GetUnit(int ID)
	{
		Iterator<Unit> u = units.iterator();
		while (u.hasNext()) {
			Unit unit = u.next();
			if (unit.GetID() == ID) {
				return unit;
			}
		}
		
		return null;
	}
	
	public void Update(Vector2 Campos)
	{
		if (targetpos >= 0)
			Move(Campos);
	}
	
	private void ModTarget()
	{
		// -1 means don't move at all
		if (targetpos < 0) return;
		
		// check the distance to the target in each direction
		float rdist = (Game.WORLDW-(bbox.x+bbox.width))+targetpos;
		if (targetpos > bbox.x+bbox.width)
			rdist = targetpos -(bbox.x+bbox.width);

		float ldist = bbox.x + (Game.WORLDW-targetpos);
		if (targetpos < bbox.x)
			ldist = (bbox.x - targetpos);
		
		// modify the target position so its facing the front of the squad
		if (rdist < ldist)
			targetpos -= (units.size()-1)*squadspacing;
		
		// set within the world bounds
		if (targetpos < 0) targetpos += Game.WORLDW;
		else if (targetpos >= Game.WORLDW) targetpos -= Game.WORLDW;
	}
	
	private int GetTargetDirection()
	{
		float rdist = (Game.WORLDW-(bbox.x+bbox.width)) + targetpos;
		if (targetpos > bbox.x)
			rdist = targetpos - (bbox.x);
		
		float ldist = bbox.x + (Game.WORLDW - targetpos);
		if (targetpos < bbox.x)
			ldist = (bbox.x - targetpos);
		
		if (rdist < ldist)
			return 1;
		else if (ldist < rdist)
			return -1;
		else
			return 0;
	}
	
	private int GetMoveDirection(Unit U, int TargetPos)
	{
		Vector2 pos = U.GetPos();
		int width = U.GetWidth();


		// check the distance to the target in each direction
		float rdist = (Game.WORLDW-(pos.x+width)) + TargetPos;
		if (TargetPos > pos.x)
			rdist = TargetPos -(pos.x);

		float ldist = pos.x + (Game.WORLDW - TargetPos);
		if (TargetPos < pos.x)
			ldist = (pos.x - TargetPos);
		
		if (rdist < ldist)
			return 1;
		else if (ldist < rdist)
			return -1;
		else
			return 0;
	}
	
	public void Move(Vector2 Campos)
	{
		// calculate the bounding box
		CalcBoundingBox(Campos);
		
		// for each unit in this squad
		Iterator<Unit> i = units.iterator();
		int index = -1; // start at 0
		int updated = 0;
		
		// for each unit
		while (i.hasNext())
		{
			Unit u = i.next();
			index++; // start at 0
			
			int target = targetpos + index*squadspacing;
			if (target < 0) target += Game.WORLDW;
			if (target >= Game.WORLDW) target -= Game.WORLDW;
			int direction = GetMoveDirection(u, target);
			
			// check if this unit has reached his position
			if (target >= u.GetPos().x && target <= u.GetPos().x+u.GetWidth())
				continue;
		
			// really not good code to fix a rare problem
			if (direction == -1 && u.IsForward() && ismoving)
				continue;
			if (direction == 1 && !u.IsForward() && ismoving)
				continue;
			
			// move the unit
			updated++;
			if (direction == -1)
				u.MoveLeft();
			else if (direction == 1)
				u.MoveRight();
		}
		
		// if they have all met their positional conditional, stop moving them
		if (updated == 0 && ismoving) {
			CalcBoundingBox(Campos);
			ismoving = false;
			
			// add a fox hole when done moving
			if (addfox) {
				GetArmy().AddFox(foxpos);
				addfox = false;
			}
			
			// send a message of units position to clients
			i = units.iterator();
			while (i.hasNext()) {
				Unit u = i.next();
				
				Response r = new Response();
				r.source = GetArmy().GetConnection();
				r.request = "UNITPOSITION";
				r.i0 = GetID();
				r.i1 = u.GetID();
				r.f0 = u.GetPos().x;
				r.f1 = u.GetPos().y;
				GetArmy().GetNetwork().GetClient().sendTCP(r);
			}
			
		} else if (updated > 0) {
			ismoving = true;
		}
	}
	
	public void DrawView(Camera Cam)
	{
		Iterator<Unit> i = units.iterator();
		while (i.hasNext()) {
			Unit u = i.next();
			Vector2 pos = new Vector2( u.GetPos() );
			pos.x += u.GetWidth()/2;
			pos.y += u.GetHeight()/2;
			
			FogOfWar.AddVisibleRegion(Cam.GetRenderX(pos.x), 
					Cam.GetRenderY(pos.y), arms.GetRange());
		}
	}
	
	private void SetPointerHeight()
	{
		// increment the pointer height
		if (direction) {
			pointerheight += Gdx.graphics.getDeltaTime()*16;
		} else {
			pointerheight -= Gdx.graphics.getDeltaTime()*16;
		}
		
		// switch direction at the bounds 
		if (pointerheight > MAXHEIGHT) {
			pointerheight = MAXHEIGHT;
			direction = false;
		} else if (pointerheight < 0) {
			pointerheight = 0;
			direction = true;
		}
	}
	
	public void DrawMugshots(SpriteBatch Batch, int XPos, int YPos)
	{
		int i = 0;
		Iterator<Unit> u = units.iterator();
		while (u.hasNext()) {
			int index = u.next().GetMugShotIndex();
			if (index < 0) index = 0;
			else if (index >= MUGSHOTCOUNT)
				index = MUGSHOTCOUNT-1;
			int offset = (32+SPACING)*i;
			
			Batch.draw(mugshots[index], XPos+offset, YPos);
			i++;
		}
	}
	
	public void DrawTargetSquad(SpriteBatch Batch, Camera Cam)
	{
		if (isFiring && units.size() > 0) {
			Iterator<Unit> u = units.iterator();
			while (u.hasNext()) {
				u.next().DrawTargetAngle(Batch, Cam);
			}
			
			return;
		}
		
		// a target squad must be set
		if (targetsquad == null)
			return;
		
		float xpos = targetsquad.GetBBox().x + targetsquad.GetBBox().width/2f;
		float ypos = ter.GetHeight((int)xpos) - targetsquad.GetBBox().height;
		SetPointerHeight();
		
		Batch.draw(pointer, Cam.GetRenderX(xpos), Cam.GetRenderY(Game.WORLDH-ypos + (float)pointerheight));
	}
	
	public void DrawTargetPos(SpriteBatch Batch, Camera Cam)
	{
		// do not draw the target pointer if the the target position is met
		if (targetpos < 0)
			return;
		
		int xpos = targetpos;
		
		// if moving right, modify the xpos
		int direction = GetTargetDirection();
		if (direction == 1 && units.size() > 1)
			xpos += bbox.width - units.get(0).GetWidth();
		
		// set to world bounds
		if (xpos > Game.WORLDW)
			xpos -= Game.WORLDW;
		else if (xpos < 0)
			xpos += Game.WORLDW;
		
		// check if this unit has reached his position
		if (xpos >= minx && xpos <= maxx)
			return;
		
		SetPointerHeight();
		int ypos = ter.GetHeight(xpos);
		Batch.draw(pointer, Cam.GetRenderX(xpos), Cam.GetRenderY(Game.WORLDH-ypos + (float)pointerheight));
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam, boolean Highlight)
	{
		// draw and determine whether this squad is forward or not
		int forwardc = 0;
		isforward = false;
		
		Iterator<Unit> i = units.iterator();
		while (i.hasNext()) {
			Unit u = i.next();
			
			// tell the unit whether or not it should be in firing position
			u.SetFiring(isFiring || targetsquad != null);
			u.Draw(Batch, Cam, Highlight, isTarget);
			
			if (u.IsForward() && !isforward) {
				forwardc++;
				if (forwardc > units.size()/2) {
					isforward = true;
				}
			}
		}
		
		// must manually be set to true each frame by the squad who is targeting
		isTarget = false;
	}
}
