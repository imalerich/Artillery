package ui;

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
	
	public void SetSelected(Squad Selected)
	{
		move.SetPos((int)Selected.GetBoundingBox().x, (int)Selected.GetBoundingBox().width);
		move.SetMaxDist(Selected.GetMoveDist());
		startx = Selected.GetBoundingBox().x + Selected.GetBoundingBox().width/2f;
	}
	
	public void SetSelectedTarget(Squad Selected)
	{
		int d = GetMoveDirection(pos.x);
		if (d == 1) {
			Selected.SetTargetX((int)pos.x + FoxHole.FOXHOLE.getWidth());
		} else {
			Selected.SetTargetX((int)pos.x);
		}
	}
	
	public boolean IsPosValid()
	{
		return isvalid;
	}
	
	public Vector2 GetPos()
	{
		return pos;
	}
	
	public static void CutRoom(Terrain Ter, Vector2 Pos)
	{
		int ypos = Game.WORLDH - (int)Pos.y;
		Ter.CutRegion((int)Pos.x+FoxHole.MOUNDWIDTH, 0, FoxHole.FOXHOLE.getWidth()-(FoxHole.MOUNDWIDTH*2), ypos);
	}
	
	public void Update(Camera Cam)
	{
		float xpos = Cursor.GetMouseX(Cam.GetPos()) + Cam.GetPos().x - FoxHole.FOXHOLE.getWidth()/2;
		xpos = ValidatePos(Cam.GetPos(), xpos);
	
		// a change in position occurred
		if (pos.x != xpos) {
			// set the new position
			pos.x = xpos;
			
			// calculate the variation
			int miny = ter.GetMinHeight( (int)pos.x, (int)(pos.x + FoxHole.FOXHOLE.getWidth()) );
			int maxy = ter.GetMaxHeight( (int)pos.x, (int)(pos.x + FoxHole.FOXHOLE.getWidth()) );
			int variation = maxy -miny;
			
			pos.y = Game.WORLDH - maxy - 1;
			
			if (variation < DELTA_THRESHOLD) {
				isvalid = true;
			} else {
				isvalid = false;
			}
		}
	}
	
	public float ValidatePos(Vector2 Campos, float Pos)
	{
		float minx = move.GetMinX();
		float maxx = move.GetMaxX();
		
		int d = GetMoveDirection(Pos);
		
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
	
	private int GetMoveDirection(float Pos)
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
	
	public void Render(SpriteBatch Batch, Camera Cam)
	{
		// set the indication color on whether or not the position is valid
		if (!isvalid) 
			Batch.setColor(1f, 0f, 0f, 0.6f);
		else 
			Batch.setColor(0f, 1f, 0f, 0.6f);
		
		Batch.draw(FoxHole.FOXHOLE, Cam.GetRenderX(pos.x), Cam.GetRenderY(pos.y));
		Batch.setColor(Color.WHITE);
	}
}
