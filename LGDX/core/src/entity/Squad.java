package entity;

import java.util.Iterator;
import java.util.Vector;

import terrain.FogOfWar;
import terrain.Terrain;
import ammunition.Armament;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Cursor;
import com.mygdx.game.Game;

public class Squad 
{
	private static Texture pointer;
	private static int MAXHEIGHT = 12;
	private int squadspacing = 32;
	
	private Armament arms;
	private Vector<Unit> units;
	private Rectangle bbox;
	private Terrain ter;
	
	private int targetpos;
	private boolean ismoving;
	
	private boolean direction;
	private double pointerheight;
	
	public static void Init()
	{
		if (pointer == null)
			pointer = new Texture( Gdx.files.internal("img/pointer.png") );
	}
	
	public static void Release()
	{
		if (pointer != null)
			pointer.dispose();
	}
	
	public Squad(Terrain Ter)
	{
		units = new Vector<Unit>();
		bbox = new Rectangle(0, 0, Float.MAX_VALUE, Float.MAX_VALUE);
		
		ter = Ter;
		targetpos = -1;
		ismoving = false;
		
		pointerheight = (int)(Math.random()*MAXHEIGHT);
		direction = true; // up
	}
	
	public void SetArmament(Armament Arms)
	{
		arms = Arms;
	}
	
	public Armament GetArmament()
	{
		return arms;
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
	
	public Rectangle GetBoundingBox()
	{
		return bbox;
	}
	
	private void CalcBoundingBox(Vector2 Campos)
	{
		// set to max to guarantee override
		float miny = Float.MAX_VALUE;
		float maxy = Float.MIN_VALUE;
		
		float maxh = Float.MIN_VALUE;
		float maxw = Float.MIN_VALUE;
		
		Iterator<Unit> i = units.iterator();
		while (i.hasNext())
		{
			// convert he position to screen coordinates
			Unit n = i.next();
			Vector2 pos = new Vector2( n.GetPos() );
			
			if (pos.y < miny)
				miny = pos.y;
			if (pos.y > maxy)
				maxy = pos.y;
			
			int height = n.GetHeight();
			if (height > maxh)
				maxh = height;
			
			int width = n.GetWidth();
			if (width > maxw)
				maxw = width;
		}
		
		// construct the new bounding box
		float minx = units.firstElement().GetPos().x;
		bbox = new Rectangle(minx, miny, (units.size()-1)*squadspacing + maxw, maxy-miny + maxh);
	}
	
	public boolean IsMouseOver(Vector2 Campos)
	{
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
	
	public void AddUnit(Unit Add, Camera Cam)
	{
		// get the position at which to add this unit
		Vector2 addp =  new Vector2(Add.GetPos());
		/*if (units.size() > 0) {
			addp = new Vector2(units.lastElement().GetPos());
			addp.x += squadspacing;
		}*/
		
		Add.SetPos(addp);
		Add.SetHeight();
		units.add(Add);
		
		CalcBoundingBox(Cam.GetPos());
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
		if (updated == 0) {
			CalcBoundingBox(Campos);
			ismoving = false;
		} else ismoving = true;
		
		// recalculate the bounding box
		CalcBoundingBox(Campos);
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
					Cam.GetRenderY(pos.y), u.GetViewRadius());
		}
	}
	
	private void SetPointerHeight()
	{
		// increment the pointer height
		if (direction)
			pointerheight += Gdx.graphics.getDeltaTime()*16;
		else
			pointerheight -= Gdx.graphics.getDeltaTime()*16;
		
		// switch direction at the bounds 
		if (pointerheight > MAXHEIGHT) {
			pointerheight = MAXHEIGHT;
			direction = false;
		} else if (pointerheight < 0) {
			pointerheight = 0;
			direction = true;
		}
	}
	
	public void DrawTarget(SpriteBatch Batch, Camera Cam)
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
		
		if (bbox.x <= xpos && bbox.x+bbox.width >= xpos)
			return;
		
		if (bbox.x <= xpos+Game.WORLDW && bbox.x+bbox.width >= xpos+Game.WORLDW)
			return;
		
		SetPointerHeight();
		int ypos = ter.GetHeight(xpos);
		Batch.draw(pointer, Cam.GetRenderX(xpos), Cam.GetRenderY(Game.WORLDH-ypos + (float)pointerheight));
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam, boolean Highlight)
	{
		Iterator<Unit> i = units.iterator();
		while (i.hasNext())
			i.next().Draw(Batch, Cam, Highlight);
	}
}
