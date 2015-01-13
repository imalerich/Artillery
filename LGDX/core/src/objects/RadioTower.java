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

public class RadioTower 
{
	public static Texture Tower;
	private static AnimTex flag;
	
	private final Vector2 pos;
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
	
	public static void release()
	{
		if (Tower != null)
			Tower.dispose();
		
		if (flag != null)
			flag.release();
	}
	
	public RadioTower(GameWorld World, Vector2 Pos, int Logo, int ID)
	{
		World.removeOutpostMarker(ID);
		pos = Pos;
		logo = Logo;
		id = ID;
	}
	
	public int getID()
	{
		return id;
	}
	
	public void draw(SpriteBatch Batch, Camera Cam)
	{
		time += Gdx.graphics.getDeltaTime();
		
		Batch.setColor(Background.BGCOLOR);
		flag.setTime(time);
		flag.render(Batch, Cam, 0, new Vector2(pos.x+6, pos.y), 1f, 1f);
		
		Batch.draw(Tower, Cam.getRenderX(pos.x), Cam.getRenderY(pos.y));
		Batch.setColor(Color.WHITE);
		
		drawLogo(Batch, Cam);
	}
	
	public void drawLogo(SpriteBatch Batch, Camera Cam)
	{
		// draw the flags logo
		Batch.setColor(Background.FGCOLOR);
		Batch.draw(MilitaryBase.logos[logo], Cam.getRenderX(6+pos.x+MilitaryBase.LOGOOFFSETX),
				Cam.getRenderY(pos.y+MilitaryBase.LOGOOFFSETY));
		Batch.setColor(Color.WHITE);
	}
}
