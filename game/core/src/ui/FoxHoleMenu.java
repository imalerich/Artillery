package ui;

import network.Response;
import objects.FoxHole;
import terrain.Terrain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Cursor;
import com.mygdx.game.Game;

import entity.Squad;

public class FoxHoleMenu 
{
	private static final int DELTA_THRESHOLD = 32;
	private final Terrain ter;
	private Vector2 pos;
	private boolean isvalid;
	private PointSelect move;
	private float startx = 0;
	
	public FoxHoleMenu(Terrain Ter)
	{
		ter = Ter;
		pos = new Vector2();
		isvalid = false;
		move = new PointSelect(Ter);
	}
	
	public void setSelected(Squad Selected)
	{
		move.setPos((int)Selected.getBoundingBox().x, (int)Selected.getBoundingBox().width);
		move.setMaxDist(Selected.getMoveDist());
		startx = Selected.getBoundingBox().x + Selected.getBoundingBox().width/2f;
	}
	
	public void setSelectedTarget(Squad Selected)
	{
		int d = getMoveDirection(pos.x);
		int xpos = 0;
		if (d == 1) {
			xpos = (int)pos.x + FoxHole.FOXHOLE.getWidth() - FoxHole.MOUNDWIDTH;
		} else {
			xpos = (int)pos.x + FoxHole.MOUNDWIDTH;
		}
		
		// tell all clients which squad is moving
		Response r = new Response();
		r.request = "SQUADMOVE";
		r.i0 = Selected.getID();
		r.i1 = xpos;
		r.source = Selected.getArmy().getConnection();
		
		if (Game.NETWORKED)
			Selected.getArmy().getNetwork().getUserClient().sendTCP(r);
		Selected.setTargetX(xpos);
		Selected.addFoxOnFinishMove(new Vector2(pos), true);
	}
	
	public boolean isPosValid()
	{
		return isvalid;
	}
	
	public Vector2 getPos()
	{
		return new Vector2(pos);
	}
	
	public static void cutRoom(Terrain Ter, Vector2 Pos)
	{
		int ypos = Game.WORLDH - (int)Pos.y;
		Ter.cutRegion((int)Pos.x+FoxHole.MOUNDWIDTH, 0, FoxHole.FOXHOLE.getWidth()-(FoxHole.MOUNDWIDTH*2), ypos);
	}
	
	public void update(Camera Cam)
	{
		float xpos = Cursor.getMouseX(Cam.getPos()) + Cam.getPos().x - FoxHole.FOXHOLE.getWidth()/2;
		xpos = validatePos(Cam.getPos(), xpos);
	
		// a change in position occurred
		if (pos.x != xpos) {
			// set the new position
			pos.x = xpos;
			
			// calculate the variation
			int miny = ter.getMinHeight( (int)pos.x, (int)(pos.x + FoxHole.FOXHOLE.getWidth()) );
			int maxy = ter.getMaxHeight( (int)pos.x, (int)(pos.x + FoxHole.FOXHOLE.getWidth()) );
			int variation = maxy -miny;
			
			pos.y = Game.WORLDH - maxy - 1;
			
			if (variation < DELTA_THRESHOLD) {
				isvalid = true;
			} else {
				isvalid = false;
			}
		}
	}
	
	public float validatePos(Vector2 Campos, float Pos)
	{
		float minx = move.getMinX();
		float maxx = move.getMaxX();
		
		int d = getMoveDirection(Pos);
		
		if (d == 1) {
			// right
			if (maxx < Game.WORLDW) {
				if (Pos > maxx) {
					return maxx;
				} else if (Pos < startx){
					Pos = maxx;
				}
			} else if (maxx >= Game.WORLDW) {
				if (Pos > startx && Pos < Game.WORLDW) {
					return Pos;
				} else if (Pos < startx && Pos > maxx - Game.WORLDW){
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
		float width = FoxHole.FOXHOLE.getWidth()/2f;

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
		// set the indication color on whether or not the position is valid
		if (!isvalid) 
			Batch.setColor(1f, 0f, 0f, 0.6f);
		else 
			Batch.setColor(0f, 1f, 0f, 0.6f);
		
		Batch.draw(FoxHole.FOXHOLE, Cam.getRenderX(pos.x), Cam.getRenderY(pos.y));
		Batch.setColor(Color.WHITE);
	}
}
