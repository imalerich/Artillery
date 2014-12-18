package ui;

import objects.RadioTower;
import terrain.Background;
import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.AnimTex;
import com.mygdx.game.Camera;
import com.mygdx.game.Cursor;
import com.mygdx.game.Game;
import com.mygdx.game.Shaders;

public class OutpostFlag 
{
	private static AnimTex tex;
	private float time = 0f;
	private final Vector2 pos;
	private Rectangle bbox;
	
	public static void init()
	{
		tex = new AnimTex("img/ui/indicators/radioflag.png", 1, 3, 1);
		tex.newAnimation(0, 3, 0, 2, 0.111f);
	}
	
	public static void release()
	{
		if (tex != null)
			tex.release();
	}
	
	public OutpostFlag(int XPos, Terrain Ter)
	{
		int xpos = XPos;
		
		// generate the base data
		int ypos = Ter.getMaxHeight(xpos, xpos+RadioTower.Tower.getWidth());
		int max = Ter.getMinHeight(xpos, xpos+RadioTower.Tower.getWidth());
		Ter.cutRegion(xpos, ypos, RadioTower.Tower.getWidth(), ypos-max);
		ypos = Game.WORLDH - Ter.getHeight(0) - 3;
		
		pos = new Vector2(xpos + RadioTower.Tower.getWidth() - tex.getFrameWidth()/2, ypos);
		bbox = new Rectangle(pos.x, pos.y + 15, tex.getFrameWidth(), tex.getFrameHeight()+10);
	}
	
	private void drawOutline(SpriteBatch Batch, Camera Cam, float Offset)
	{
		Shaders.setShader(Batch, Shaders.hili);
		for (int x=-1; x<2; x++) {
			for (int y=-1; y<2; y++) {
				Vector2 Coords = new Vector2(pos);
				Coords.x += x;
				Coords.y += y;
				
				tex.render(Batch, Cam, 0, new Vector2(Coords.x, Coords.y + Offset), 1f, 1f);
			}
		}
		Shaders.revertShader(Batch);
	}
	
	public void draw(SpriteBatch Batch, Camera Cam)
	{
		time += Gdx.graphics.getDeltaTime();
		float offset = 20 + (float)Math.sin(time*2)*5;
		
		tex.setTime(time);
		
		if (Cursor.isMouseOver(bbox, Cam.getPos()))
			drawOutline(Batch, Cam, offset);
		
		Batch.setColor(Terrain.getColor());
		tex.render(Batch, Cam, 0, new Vector2(pos.x, pos.y + offset), 1f, 1f);
		Batch.setColor(Color.WHITE);
	}
}
