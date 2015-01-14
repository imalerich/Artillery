package objects;

import physics.GameWorld;
import terrain.Background;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.AnimTex;
import com.mygdx.game.Camera;
import com.mygdx.game.MilitaryBase;
import com.mygdx.game.Shaders;

import entity.Unit;

public class RadioTower extends Unit
{
	public static Texture Tower;
	private static AnimTex flag;
	
	private final int id;
	
	private float time = 0f;
	private int logo = 0;
	
	public static void init()
	{
		if (Tower == null)
			Tower = new Texture( Gdx.files.internal("img/objects/tower.png") );
		
		if (flag == null) {
			flag = new AnimTex("img/army/flag.png", 1, 3, 1);
			flag.newAnimation(0, 3, 0, 2, 0.333f/4f);
		}
	}
	
	public static void dispose()
	{
		if (Tower != null)
			Tower.dispose();
		
		if (flag != null)
			flag.release();
	}
	
	public RadioTower(GameWorld World, Vector2 Pos, int Logo, int ID)
	{
		if (ID != -1) {
			World.removeOutpostMarker(ID);
		}
		
		logo = Logo;
		id = ID;
		
		width = Tower.getWidth();
		height = Tower.getHeight();
		
		pos = new Vector2(Pos);
		forward = true;
		ter = World.getTerrain();
		speed = 0;
	}
	
	public int getID()
	{
		return id;
	}
	
	private void drawOutline(SpriteBatch Batch, Camera Cam)
	{
		for (int x=-1; x<2; x++) {
			for (int y=-1; y<2; y++) {
				Vector2 Coords = new Vector2(pos);
				Coords.x += x;
				Coords.y += y;
				
				flag.render(Batch, Cam, 0, new Vector2(Coords.x+6, Coords.y), 1f, 1f);
				Batch.draw(Tower, Cam.getRenderX(Coords.x), Cam.getRenderY(Coords.y));
			}
		}
	}
	
	private void drawTarget(SpriteBatch Batch, Camera Cam)
	{
		// draw a highlighted version of the sprite
		Shaders.setShader(Batch, Shaders.target);
		drawOutline(Batch, Cam);
		Shaders.revertShader(Batch);
	}
	
	private void drawHighlight(SpriteBatch Batch, Camera Cam)
	{
		// draw a highlighted version of the sprite
		Shaders.setShader(Batch, Shaders.hili);
		drawOutline(Batch, Cam);
		Shaders.revertShader(Batch);
	}
	
	private void drawLogo(SpriteBatch Batch, Camera Cam)
	{
		// draw the flags logo
		Batch.setColor(Background.FGCOLOR);
		Batch.draw(MilitaryBase.logos[logo], Cam.getRenderX(6+pos.x+MilitaryBase.LOGOOFFSETX),
				Cam.getRenderY(pos.y+MilitaryBase.LOGOOFFSETY));
		Batch.setColor(Color.WHITE);
	}
	
	@Override
	public void draw(SpriteBatch Batch, Camera Cam, boolean Highlight, boolean Target)
	{
		time += Gdx.graphics.getDeltaTime();
		
		Batch.setColor(Background.BGCOLOR);
		flag.setTime(time);
		
		if (Highlight) {
			drawHighlight(Batch, Cam);
		} else if (Target) {
			drawTarget(Batch, Cam);
		}
		
		flag.render(Batch, Cam, 0, new Vector2(pos.x+6, pos.y), 1f, 1f);
		Batch.draw(Tower, Cam.getRenderX(pos.x), Cam.getRenderY(pos.y));
		Batch.setColor(Color.WHITE);
		
		drawLogo(Batch, Cam);
	}
}
