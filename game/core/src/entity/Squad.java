package entity;

import java.util.Iterator;
import java.util.Vector;

import network.Response;
import objects.FoxHole;
import particles.Particles;
import physics.Blast;
import physics.Flame;
import physics.GameWorld;
import physics.NullTank;
import terrain.FogOfWar;
import terrain.Terrain;
import arsenal.Armament;
import arsenal.Armor;
import objects.RadioTower;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
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
import com.mygdx.game.Util;

public class Squad 
{
	public static Texture pointer;
	public static AnimTex target;
	private static TextureRegion[] mugshots;
	private static Texture health_f;
	private static Texture armor_f;
	private static Texture health_b;
	private static final int MUGSHOTCOUNT = 4;
	public static final int SPACING = 2;
	
	private final Classification classification;
	
	private static int MAXHEIGHT = 12;
	private int squadspacing = 24;
	private int maxmovedist = 0;
	private int id = -1;
	
	// the armor and armament that is used by this squad
	private Armament primary;
	private Armament secondary;
	private Armament offhand;
	private Armor armor;
	
	private Vector<Unit> units;
	private Rectangle bbox;
	private float minx, maxx;
	private Terrain ter;
	
	private Squad targetsquad;
	private int targetpos;
	private boolean ismoving;
	private boolean isforward;
	private boolean canBurn = false;
	
	private float powerratio = 0f;
	private boolean isFiringP = false;
	private boolean isFiringS = false;
	private boolean isFiringO = false;
	private boolean isTarget;
	private boolean direction;
	private double pointerheight;
	private int addid = 0;
	private boolean takesdirectdamage = true;
	
	private FoxHole occupied;
	private boolean addfox;
	private Vector2 foxpos;
	
	private boolean canmove = true;
	private boolean addbarrier;
	private boolean addoutpost;
	private Vector<Vector2> barrierpos;
	
	private boolean swapState = false;
	private boolean rumble = false;
	private int reqbonus = 0;
	
	private boolean hasActiveCloak = false;
	private boolean isInvis = false;
	private float opacity = 1f;
	
	// state to use when adding a unit
	private Vector2 barrelSrc = new Vector2();
	
	private int lastHitBy = -1;
	private Army army;
	
	public static void init()
	{
		if (pointer == null)
			pointer = new Texture( Gdx.files.internal("img/ui/indicators/pointer.png") );
		
		if (target == null) {
			target = new AnimTex("img/ui/indicators/target.png", 1, 4, 1);
			target.newAnimation(0, 4, 0, 3, 0.12f);
		}
		
		if (health_f == null) {
			health_f = new Texture( Gdx.files.internal("img/ui/profile/health_f.png") );
		}
		
		if (health_b == null) {
			health_b = new Texture( Gdx.files.internal("img/ui/profile/health_b.png") );
		}
		
		if (armor_f == null) {
			armor_f = new Texture( Gdx.files.internal("img/ui/profile/armor_f.png") );
		}
		
		if (mugshots == null) {
			Texture tmp = new Texture( Gdx.files.internal("img/ui/profile/mugshots.png") );
			mugshots = TextureRegion.split(tmp, 32, 32)[0];
		}
	}
	
	public static void release()
	{
		if (pointer != null)
			pointer.dispose();
		
		if (target != null)
			target.release();
		
		if (health_f != null)
			health_f.dispose();
		
		if (health_b != null)
			health_b.dispose();
		
		if (armor_f != null)
			armor_f.dispose();
	}
	
	public Squad(Terrain Ter, int MoveDist, Army A, Classification C)
	{
		classification = C;
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
		
		occupied = null;
		addfox = false;
		addbarrier = false;
		foxpos = null;
		barrierpos = null;
	}
	
	public void addLandMines(GameWorld World)
	{
		Iterator<Unit> u = units.iterator();
		while (u.hasNext()) {
			Unit unit = u.next();
			float xpos = unit.getPos().x + unit.getWidth()/2f;
			World.addLandMine(xpos, getArmy().getConnection(), secondary.getStrength());
		
			Response r = new Response();
			r.source = getArmy().getConnection();
			r.request = "ADDMINE";
			r.f0 = xpos;
			r.i0 = getArmy().getConnection();
			r.i1 = secondary.getStrength();

			if (Game.NETWORKED)
				getArmy().getNetwork().getUserClient().sendTCP(r);
		}
	}
	
	public void canBurn(boolean B)
	{
		canBurn = B;
	}
	
	public boolean doAddFox()
	{
		return addfox;
	}

	public void addOutpostOnFinishMove(boolean Add)
	{
		addoutpost = Add;
	}
	
	public boolean doAddBarrier()
	{
		return addbarrier;
	}
	
	public Classification getClassification()
	{
		return classification;
	}
	
	public void setActiveCloak(boolean S)
	{
		hasActiveCloak = S;
		isInvis = S;
	}
	
	public void setInvis(boolean S)
	{
		if (hasActiveCloak)
			isInvis = S;
		else
			isInvis = false;
	}
	
	public void setLastHitBy(int Army)
	{
		lastHitBy = Army;
	}
	
	public void setReqBonus(int Bonus)
	{
		reqbonus = Bonus;
	}
	
	public int getReqBonus()
	{
		return reqbonus;
	}
	
	public boolean canMove()
	{
		return canmove;
	}
	
	public void setCanMove(boolean CanMove)
	{
		canmove = CanMove;
	}
	
	public Army getArmy()
	{
		return army;
	}
	
	public void setID(int ID)
	{
		id = ID;
	}
	
	public int getID()
	{
		return id;
	}
	
	public void checkUnitFireDamage()
	{
		Iterator<Unit> u = units.iterator();
		while  (u.hasNext()) {
			Unit unit = u.next();
			unit.setOnFire( unit.isOnFire() - 1 );
			
			// deal fire damage
			if (unit.isOnFire() > 0) {
				unit.damage(Flame.FLAMEDMG);
			}
		}
	}
	
	public boolean isStealthed()
	{
		return (occupied != null) || (isInvis);
	}
	
	public void setBarrelSrc(Vector2 Src)
	{
		barrelSrc = Src;
		Iterator<Unit> u = units.iterator();
		while (u.hasNext())
			u.next().setBarrelSrc(Src);
	}
	
	public void setPowerRatio(float Ratio)
	{
		powerratio = Ratio;
	}
	
	public float getPowerRatio()
	{
		return powerratio;
	}
	
	public void setMaxHealth()
	{
		Iterator<Unit> u = units.iterator();
		while (u.hasNext())
		{
			u.next().setMaxHealth();
		}
	}
	
	public void addFoxOnFinishMove(Vector2 Pos, boolean S)
	{
		addfox = S;
		foxpos = Pos;
	}

	public void addBarrierOnFinishedMove(Vector<Vector2> Pos, boolean S)
	{
		addbarrier = S;
		barrierpos = Pos;
	}
	
	public void takesDirectDamage(boolean B)
	{
		takesdirectdamage = B;
		Iterator<Unit> u = units.iterator();
		while (u.hasNext())
		{
			Unit next = u.next();
			if (next != null)
				next.setDirectDamage(takesdirectdamage);
		}
	}
	
	public void checkAlive(Vector2 Campos, Vector<NullTank> Deceased, Particles Part)
	{
		Iterator<Unit> u = units.iterator();
		while (u.hasNext())
		{
			// remove all dead units
			Unit unit = u.next();
			if (!unit.isAlive()) {
				unit.setAsDeceased(Deceased, Part);
				
				// add the bonus requisition to the army
				float xpos = unit.getBBox().x + unit.getBBox().width/2f;
				float ypos = unit.getBBox().y + unit.getBBox().height;
				
				// do not give requisition when you kill yourself
				if (lastHitBy != getArmy().getConnection()) {
					army.getWorld().getRemoteArmy(lastHitBy).addRequisition(unit.getReqBonus(), new Vector2(xpos, ypos));
				}
				
				// remove the unit
				u.remove();
			}
		}
		
		calcBoundingBox();
	}
	
	public void procBlasts(Blast B)
	{
		Iterator<Unit> u = units.iterator();
		while (u.hasNext())
		{
			// process damage to units done by any blasts
			Unit unit = u.next();
				
			if (Intersector.overlaps(new Circle(B.pos, B.radius), unit.getBBox()) ||
					unit.getBBox().contains(B.pos)) {
				setLastHitBy(B.getSourceArmy());
				float dmg = Math.max(B.strength - unit.getArmor().getStrength(), 0);
				unit.getArmor().damage((int)B.strength);

				unit.damage(dmg);
			}
		}
	}
	
	public void procFlame(Flame F)
	{
		Iterator<Unit> u = units.iterator();
		while (u.hasNext())
		{
			// process damage to units done by any blasts
			Unit unit = u.next();
			
			if (Intersector.overlapConvexPolygons(Util.rectToPoly(unit.getBBox()), F.bounding)) {
				setLastHitBy(F.source);
				float dmg = Math.max(F.strength - unit.getArmor().getStrength(), 0);
				unit.getArmor().damage((int)F.strength);

				unit.damage(dmg);
				
				if (canBurn)
					unit.setOnFire(Flame.ONFIRETURNS);
			}
		}
	}
	
	public Iterator<Unit> getUnitIterator()
	{
		return units.iterator();
	}
	
	public int getMoveDist()
	{
		return maxmovedist;
	}
	
	public boolean isForward()
	{
		return isforward;
	}
	
	public void setFiringPrimary(boolean IsFiring)
	{
		isFiringP = IsFiring;
	}
	
	public void setFiringSecondary(boolean IsFiring)
	{
		isFiringS = IsFiring;
	}
	
	public void setFiringOffhand(boolean IsFiring)
	{
		isFiringO = IsFiring;
	}
	
	public boolean isFiringPrimary()
	{
		return isFiringP;
	}
	
	public boolean isFiringSecondary()
	{
		return isFiringS;
	}
	
	public boolean isFiringOffhand()
	{
		return isFiringO;
	}
	
	public void setAsTarget()
	{
		isTarget = true;
	}
	
	public Vector<Unit> getUnits()
	{
		return units;
	}
	
	public Rectangle getBBox()
	{
		return bbox;
	}
	
	public void setPrimary(Armament Arms)
	{
		primary = new Armament(Arms);
	}
	
	public void setSecondary(Armament Arms)
	{
		secondary = new Armament(Arms);
	}
	
	public void setOffhand(Armament Arms)
	{
		offhand = new Armament(Arms);
	}
	
	public Armament getPrimary()
	{
		return primary;
	}
	
	public Armament getSecondary()
	{
		return secondary;
	}
	
	public Armament getOffhand()
	{
		return offhand;
	}
	
	public void setArmor(Armor Set)
	{
		armor = new Armor(Set);
		Iterator<Unit> u = units.iterator();
		while (u.hasNext())
			u.next().setArmor(armor);
	}
	
	public Armor getArmor()
	{
		return armor;
	}
	
	public int getTargetX()
	{
		return targetpos;
	}
	
	public void setTargetX(int Target)
	{
		targetpos = Target;
		modTarget();
	}
	
	public Squad getTargetSquad()
	{
		return targetsquad;
	}
	
	public void setTargetSquad(Squad Target)
	{
		targetsquad = Target;
		if (targetsquad == null)
			return;
		
		// face the target squad
		int direction = GameWorld.getDirection(bbox.x, 1, 
				targetsquad.getBBox().x, 1);
		
		Iterator<Unit> u = units.iterator();
		while (u.hasNext()) {
			u.next().setForward(direction == 1);
		}
	}
	
	public void setSwapState(boolean B)
	{
		swapState = B;
	}
	
	public boolean doSwapState()
	{
		return swapState;
	}
	
	public void swapState()
	{
		if (swapState)
			canmove = !canmove;
	}
	
	public Rectangle getBoundingBox()
	{
		return bbox;
	}
	
	public int getUnitCount()
	{
		return units.size();
	}
	
	private void rumbleCam(Camera Cam)
	{
		// get the distance from the point of fire to the center of the screen
		float dist = Vector2.dst(bbox.x + bbox.getWidth()/2f, bbox.y + bbox.getHeight()/2f, 
				Cam.getPos().x + Game.SCREENW/2f, Cam.getPos().y + Game.SCREENH/2f);
		float mag = 1f;
		if (dist > Game.SCREENW)
			mag = 0f;
		else if (dist > 0f) {
			mag *= (Game.SCREENW-dist)/(Game.SCREENW);
		}

		rumble = true;
		Cam.setRumble(mag);
	}
	
	private void calcBoundingBox()
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
		float firstx = units.get(0).getPos().x;
		
		Iterator<Unit> i = units.iterator();
		while (i.hasNext())
		{
			// convert he position to screen coordinates
			Unit n = i.next();
			Rectangle r = n.getBBox();
			
			// used for determing whether or not to draw the target position
			if (n.getPos().x < minx)
				minx = n.getPos().x;
			if (n.getPos().x + n.getWidth() > maxx)
				maxx = n.getPos().x + n.getWidth();
			
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
	
	public boolean isIntersectingView(Rectangle R)
	{
		// find if any of the squads units view overlaps the target
		Iterator<Unit> u = getUnitIterator();
		while (u.hasNext()) {
			Unit unit = u.next();
			Circle c = new Circle(unit.getPos().x + unit.getWidth()/2, 
					unit.getPos().y + unit.getHealth()/2, primary.getRange());

			if (Intersector.overlaps(c, R)) {
				return true;
			}
		}
		
		return false;
	}
	
	public void setForward(boolean Forward)
	{
		Iterator<Unit> i = units.iterator();
		while (i.hasNext()) {
			i.next().setForward(Forward);
		}
	}
	
	public boolean isMouseOver(Vector2 Campos)
	{
		calcBoundingBox();
		return Cursor.isMouseOver(bbox, Campos);
	}
	
	public boolean isMoving()
	{
		return ismoving;
	}
	
	public void setSquadSpacing(int SquadSpacing)
	{
		squadspacing = SquadSpacing;
	}
	
	public int getSquadSpacing()
	{
		return squadspacing;
	}
	
	public void setBarrelAngle(float Angle)
	{
		// all squad members must have the same angle
		Iterator<Unit> u = units.iterator();
		while (u.hasNext()) {
			Unit unit = u.next();
			
			unit.setBarrelAngle(Angle);
			if (primary != null)
				primary.setAngle(unit.getBarrelAbsoluteAngle());
			
			if (secondary != null)
				secondary.setAngle(unit.getBarrelAbsoluteAngle());
		}
	}
	
	public void addUnit(Unit Add)
	{
		// set the default barrel src to the middle of the unit
		if (units.size() == 0 && barrelSrc.x == 0f && barrelSrc.y == 0f) {
			barrelSrc = new Vector2(Add.getWidth()/2f, Add.getHeight()/2f);
		}
		
		// get the position at which to add this unit
		Vector2 addp =  new Vector2(Add.getPos());
		
		Add.getPos(addp);
		Add.setHeight();
		units.add(Add);
		units.lastElement().setID(addid);
		units.lastElement().setDirectDamage(takesdirectdamage);
		units.lastElement().setBarrelSrc(barrelSrc);
		units.lastElement().setSquad(this);
		units.lastElement().setArmor(armor);
		addid++;
		
		calcBoundingBox();
	}
	
	public Unit getUnit(int ID)
	{
		Iterator<Unit> u = units.iterator();
		while (u.hasNext()) {
			Unit unit = u.next();
			if (unit.getID() == ID) {
				return unit;
			}
		}
		
		return null;
	}
	
	public void update(Camera Cam)
	{
		if (targetpos >= 0)
			move(Cam);
	}
	
	private void modTarget()
	{
		// -1 means don't move at all
		if (targetpos < 0) return;
		
		// modify the target position so its facing the front of the squad
		if (getDirection() == 1)
			targetpos -= (units.size()-1)*squadspacing;
		
		// set within the world bounds
		if (targetpos < 0) targetpos += Game.WORLDW;
		else if (targetpos >= Game.WORLDW) targetpos -= Game.WORLDW;
	}
	
	public int getWidth()
	{
		return (units.size()-1)*squadspacing;
	}
	
	public int getDirection()
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
	
	private int getMoveDirection(Unit U, int TargetPos)
	{
		Vector2 pos = U.getPos();
		int width = U.getWidth();


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
	
	public void move(Camera Cam)
	{
		// calculate the bounding box
		calcBoundingBox();
		getArmy().getWorld().checkLandMines(bbox, getArmy().getConnection());
		
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
			int direction = getMoveDirection(u, target);
			
			// check if this unit has reached his position
			if (target >= u.getPos().x && target <= u.getPos().x+u.getWidth())
				continue;
		
			// really not good code to fix a rare problem
			if (direction == -1 && u.isForward() && ismoving)
				continue;
			if (direction == 1 && !u.isForward() && ismoving)
				continue;
			
			// move the unit
			updated++;
			if (direction == -1)
				u.moveLeft(Cam);
			else if (direction == 1)
				u.moveRight(Cam);
			
			// regain cloak on movement
			setInvis(true);
		}
		
		if (updated > 0) {
			// set the occupied fox hole as not occupied
			if (occupied != null) {
				occupied.setOccupied(null);
				occupied = null;
			}
		}
		
		// if they have all met their positional conditional, stop moving them
		if (updated == 0 && ismoving) {
			calcBoundingBox();
			ismoving = false;
			finishedMoving(Cam.getPos());
			
			if (primary != null) {
				if (primary.getType() == Armament.POINTTARGET && rumble) {
					rumble = false;
					Cam.setRumble(0f);
				}
			}
			
		} else if (updated > 0) {
			ismoving = true;
			
			if (primary != null) {
				if (primary.getType() == Armament.POINTTARGET &&
						getArmy() instanceof UserArmy) {
					rumbleCam(Cam);
				}
			}
		}
	}
	
	public void setUnoccupiedFox()
	{
		occupied = null;
	}
	
	private void finishedMoving(Vector2 Campos)
	{
		// add a fox hole when done moving
		if (addfox) {
			getArmy().addFox(foxpos);
			addfox = false;
			foxpos = null;
		} 
		
		 if (addoutpost) {
			Vector2 v = new Vector2(getBBox().x + getBBox().width/2f - RadioTower.Tower.getWidth()/2f, 0);
			RadioTower t = new RadioTower(getArmy().getWorld(), v, getArmy().base.getLogo());
			t.setTowerSquad(this);
			getArmy().removeSquad(id);
			getArmy().addTower(t);
			addoutpost = false;

			// inform clients of the added tower
			Response r = new Response();
			r.source = getArmy().getConnection();
			r.squad = id;
			r.request = "ADDTOWER";
			r.i0 = getArmy().base.getLogo();
			r.f0 = v.x;
			r.f1 = v.y;

			if (Game.NETWORKED)
				getArmy().getNetwork().getUserClient().sendTCP(r);
		}
		
		if (addbarrier) 
		{ 
			Iterator<Vector2> i = barrierpos.iterator(); 
			while (i.hasNext())
				getArmy().addBarricade( new Vector2(i.next()) );
			
			addbarrier = false;
			barrierpos = null;
		}
		
		checkIfOccupiesFox(Campos);
		
		// send a message of units position to clients
		Iterator<Unit> i = units.iterator();
		while (i.hasNext()) {
			Unit u = i.next();

			Response r = new Response();
			r.source = getArmy().getConnection();
			r.request = "UNITPOSITION";
			r.i0 = getID();
			r.i1 = u.getID();
			r.f0 = u.getPos().x;
			r.f1 = u.getPos().y;

			if (Game.NETWORKED)
				getArmy().getNetwork().getUserClient().sendTCP(r);
		}
	}
	
	public void checkIfOccupiesFox(Vector2 Campos)
	{
		// heavy units can not be in fox holes
		if (primary.getType() == Armament.POINTTARGET) {
			return;
		}
		
		calcBoundingBox();
		
		// check if this squad now occupied a fox hole
		Iterator<FoxHole> f = getArmy().getWorld().getFoxHoles();
		while (f.hasNext()) {
			
			// cannot occupy an already occupied position
			FoxHole h = f.next();
			if (h.isOccupied()) {
				continue;
			}
			
			// if this squad overlaps the fox hole, set this fox hole as the occupied
			if ( bbox.overlaps(h.getBBox()) || bbox.contains(h.getBBox()) ) {
				h.setOccupied(this);
				occupied = h;
			
				return;
			}
		}
	}
	
	public void drawView(Camera Cam)
	{
		Iterator<Unit> i = units.iterator();
		while (i.hasNext()) {
			Unit u = i.next();
			Vector2 pos = new Vector2( u.getPos() );
			pos.x += u.getWidth()/2;
			pos.y += u.getHeight()/2;
			
			FogOfWar.addVisibleRegion(Cam.getRenderX(pos.x), 
					Cam.getRenderY(pos.y), primary.getRange());
		}
	}
	
	public void drawEnemyView(Camera Cam)
	{
		Iterator<Unit> i = units.iterator();
		while (i.hasNext()) {
			Unit u = i.next();
			
			// only provide vision when the unit is on fire
			if (u.isOnFire() == 0)
				continue;
			
			float radius = Math.max(u.getWidth(), u.getHeight())/2f;
			Vector2 pos = new Vector2( u.getPos() );
			pos.x += u.getWidth()/2;
			pos.y += u.getHeight()/2;
			
			FogOfWar.addVisibleRegion(Cam.getRenderX(pos.x), Cam.getRenderY(pos.y), (int)radius);
		}
	}
	
	private void setPointerHeight()
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
	
	public void drawMugshots(SpriteBatch Batch, int XPos, int YPos)
	{
		int i = 0;
		Iterator<Unit> u = units.iterator();
		while (u.hasNext()) {
			Unit unit = u.next();
			int index = unit.getMugShotIndex();
			if (index < 0) index = 0;
			else if (index >= MUGSHOTCOUNT)
				index = MUGSHOTCOUNT-1;
			int offset = (32+SPACING)*i;
			
			Batch.draw(mugshots[index], XPos+offset, YPos-3);
			Batch.draw(health_b, XPos+offset,YPos-health_b.getHeight()-3);
			Batch.draw(health_f, XPos+offset+2, YPos-health_b.getHeight()-1, 
					(int)(unit.getHealthPercentage()*health_f.getWidth()), health_f.getHeight(), 0, 0, 
					(int)(unit.getHealthPercentage()*health_f.getWidth()), health_f.getHeight(), false, false);
			
			Batch.draw(health_b, XPos+offset,YPos - (health_b.getHeight()*2) - 3);
			Batch.draw(armor_f, XPos+offset+2, YPos - (health_b.getHeight()*2) - 1, 
					(int)(unit.getArmorPercentage()*health_f.getWidth()), health_f.getHeight(), 0, 0, 
					(int)(unit.getArmorPercentage()*health_f.getWidth()), health_f.getHeight(), false, false);
			i++;
		}
	}
	
	public void drawTargetSquad(SpriteBatch Batch, Camera Cam)
	{
		if ((isFiringPrimary() || isFiringSecondary() || isFiringOffhand()) && units.size() > 0) {
			Iterator<Unit> u = units.iterator();
			while (u.hasNext()) {
				u.next().drawTargetAngle(Batch, Cam);
			}
			
			return;
		}
		
		// a target squad must be set
		if (targetsquad == null)
			return;
		
		float xpos = targetsquad.getBBox().x + targetsquad.getBBox().width/2f;
		float ypos = ter.getHeight((int)xpos) - targetsquad.getBBox().height;
		setPointerHeight();
		
		Batch.draw(pointer, Cam.getRenderX(xpos), Cam.getRenderY(Game.WORLDH-ypos + (float)pointerheight));
	}
	
	public void drawTargetPos(SpriteBatch Batch, Camera Cam)
	{
		// do not draw the target pointer if the the target position is met
		if (targetpos < 0)
			return;
		
		int xpos = targetpos;
		
		// if moving right, modify the xpos
		int direction = getDirection();
		if (direction == 1 && units.size() > 1)
			xpos += bbox.width - units.get(0).getWidth();
		
		// set to world bounds
		if (xpos > Game.WORLDW)
			xpos -= Game.WORLDW;
		else if (xpos < 0)
			xpos += Game.WORLDW;
		
		// check if this unit has reached his position
		if (xpos >= minx && xpos <= maxx)
			return;
		
		setPointerHeight();
		int ypos = ter.getHeight(xpos);
		Batch.draw(pointer, Cam.getRenderX(xpos), Cam.getRenderY(Game.WORLDH-ypos + (float)pointerheight));
	}
	
	private void setOpacity(float Target, float Rate)
	{
		if (opacity < Target) 
			opacity += Rate * Gdx.graphics.getDeltaTime();

		if (opacity > Target)
			opacity -= Rate * Gdx.graphics.getDeltaTime();
		
		if (Math.abs(opacity - Target) < 1/255f)
			opacity = Target;
	}
	
	public void draw(SpriteBatch Batch, Camera Cam, boolean Highlight)
	{
		// partially transparent to the user
		if (isStealthed() && !Highlight && (getArmy() instanceof UserArmy)) {
			setOpacity(0.6f, 1f);
		}
		
		// fully transparent to enemy units
		if (isStealthed() && !(getArmy() instanceof UserArmy)) {
			setOpacity(0.0f, 1f);
		}
		
		if (!isStealthed()) {
			setOpacity(1f, 2f);
		}
		
		Batch.setColor(1f, 1f, 1f, opacity);
		
		if (Highlight)  
			Batch.setColor(Color.WHITE);
		
		// draw and determine whether this squad is forward or not
		int forwardc = 0;
		isforward = false;
		
		Iterator<Unit> i = units.iterator();
		while (i.hasNext()) {
			Unit u = i.next();
			
			// tell the unit whether or not it should be in firing position
			u.setFiring((isFiringPrimary()) || targetsquad != null);
			u.draw(Batch, Cam, Highlight, isTarget);
			
			if (u.isForward() && !isforward) {
				forwardc++;
				if (forwardc > units.size()/2) {
					isforward = true;
				}
			}
		}
		
		// must manually be set to true each frame by the squad who is targeting
		isTarget = false;
		
		if (isStealthed()) {
			Batch.setColor(Color.WHITE);
		}
	}
}
