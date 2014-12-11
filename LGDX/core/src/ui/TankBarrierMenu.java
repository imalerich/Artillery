package ui;

import java.util.Iterator;
import java.util.Vector;

import network.Response;
import objects.TankBarrier;
import terrain.Terrain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Cursor;
import com.mygdx.game.Game;

import entity.Squad;

public class TankBarrierMenu 
{
	private static final int SPACING = 4;
	private final Terrain ter;
	private Vector<Vector2> pos;
	private PointSelect move;
	private float startx = 0;
	private Rectangle bbox;
	
	public TankBarrierMenu(Terrain Ter)
	{
		ter = Ter;
		move = new PointSelect(Ter);
		pos = new Vector<Vector2>();
		bbox = new Rectangle();
	}
	
	public void setSelected(Squad Selected)
	{
		move.setPos((int)Selected.getBoundingBox().x, (int)Selected.getBoundingBox().width);
		move.setMaxDist(Selected.getMoveDist());
		startx = Selected.getBoundingBox().x + Selected.getBoundingBox().width/2f;
		
		int size = Selected.getUnits().size();
		pos.clear();
		for (int i=0; i<size; i++)
			pos.add( new Vector2() );
		
		bbox.x = 0; 
		bbox.y = 0;
		bbox.width = (TankBarrier.TANKBARRIER.getWidth() + SPACING)*size - SPACING;
		bbox.height = Game.WORLDH;
	}
	
	public void setSelectedTarget(Squad Selected)
	{
		int d = getMoveDirection(bbox.x);
		int xpos = 0;
		if (d == 1) {
			xpos = (int)(bbox.x + bbox.width/2);
		} else {
			xpos = (int)(bbox.x - bbox.width/2);
		}
		
		// tell all clients which squad is moving
		Response r = new Response();
		r.request = "SQUADMOVE";
		r.i0 = Selected.getID();
		r.i1 = xpos;
		r.source = Selected.getArmy().getConnection();
		
		Selected.getArmy().getNetwork().getClient().sendTCP(r);
		Selected.setTargetX(xpos);
		Selected.addBarrierOnFinishedMove(pos);
	}
	
	public void update(Camera Cam)
	{
		int width = TankBarrier.TANKBARRIER.getWidth();
		float xpos = Cursor.getMouseX(Cam.getPos()) + Cam.getPos().x - width/2;
		xpos = validatePos(Cam.getPos(), xpos);
		bbox.x = xpos;
	
		int index = 0;
		Iterator<Vector2> i = pos.iterator();
		while (i.hasNext()) {
			Vector2 v = i.next();
			v.x = xpos + (width + SPACING) * index - bbox.width/2f;
			v.y = Game.WORLDH  - ter.getHeight((int)v.x + width/2) - 4;
			
			index++;
		}
	}
	
	public float validatePos(Vector2 Campos, float Pos)
	{
		float minx = move.getMinX();
		float maxx = move.getMaxX();
		
		int d = getMoveDirection(Pos);
		
		if (d == 1) {
			// right
			if (maxx < Game.WORLDW && Pos > maxx) {
				return maxx;
			} else if (maxx >= Game.WORLDW) {
				if (Pos > startx && Pos < Game.WORLDW) {
					return Pos;
				} else if (Pos > maxx - Game.WORLDW){
					return maxx - Game.WORLDW;
				}
			}
		} else {
			//left
			if (minx > 0 && Pos < minx) {
				return minx;
			} else if (minx < 0) {
				if (Pos < startx) {
					return Pos;
				} if (Pos < minx+Game.WORLDW) {
					return (minx+Game.WORLDW); 
				}
			}
		}
		
		return Pos;
	}
	
	private int getMoveDirection(float Pos)
	{
		float width = bbox.width/2f;

		// check the distance to the target in each direction
		float rdist = (Game.WORLDW-(startx+width)) + Pos;
		if (Pos > startx)
			rdist = Pos - (startx);

		float ldist = startx + (Game.WORLDW - Pos);
		if (Pos < startx)
			ldist = (startx - Pos);
		
		if (rdist < ldist) {
			return 1;
		} else if (ldist < rdist) {
			return -1;
		} else {
			return 0;
		}
	}
	
	public void render(SpriteBatch Batch, Camera Cam)
	{
		Batch.setColor(Terrain.getColor());
		
		Iterator<Vector2> i = pos.iterator();
		while (i.hasNext()) {
			Vector2 p = i.next();
			Batch.draw(TankBarrier.TANKBARRIER, Cam.getRenderX(p.x), Cam.getRenderY(p.y));
		}
		
		Batch.setColor(Color.WHITE);
	}
}
