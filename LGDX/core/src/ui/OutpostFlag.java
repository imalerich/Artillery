package ui;

import objects.RadioTower;
import terrain.Background;
import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.AnimTex;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;

public class OutpostFlag 
{
	private static AnimTex tex;
	private float time = 0f;
	private final Vector2 pos;
	
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
	}
	
	public void draw(SpriteBatch Batch, Camera Cam)
	{
		time += Gdx.graphics.getDeltaTime();
		float offset = 20 + (float)Math.sin(time*2)*5;
		
		Batch.setColor(Background.BGCOLOR);
		tex.setTime(time);
		tex.render(Batch, Cam, 0, new Vector2(pos.x, pos.y + offset), 1f, 1f);
		Batch.setColor(Color.WHITE);
	}
}
