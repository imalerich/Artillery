package terrain;

import java.util.Iterator;
import java.util.Vector;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.Game;

public class Sky 
{
	private static final float DENSITY = 16f; // number of stars over REGION
	private static final float REGION = 256; // region size used to determine DENSITY
	
	private static Texture tex;
	private static Vector<Vector3> stars;
	private static float alpha = 1f;
	
	public static void init()
	{
		if (tex == null)
			tex = new Texture( Gdx.files.internal("img/bg/star.png") );
		
		stars = new Vector<Vector3>();
		
		int starcount = (int)((Game.WORLDW/REGION) * (Game.WORLDH/REGION) * DENSITY);
		for (int i=0; i<starcount; i++)
			stars.add( new Vector3((float)Math.random()*Game.WORLDW, (float)Math.random()*Game.WORLDH, (float)Math.random()*0.3f));
	}
	
	public static void release()
	{
		if (tex != null)
			tex.dispose();
	}
	
	public static void setAlpha(float A)
	{
		alpha = A;
	}
	
	public static void draw(SpriteBatch Batch)
	{
		Iterator<Vector3> s = stars.iterator();
		while (s.hasNext()) {
			Vector3 star = s.next();
			
			if (star.x < Game.SCREENW && star.y < Game.SCREENH) {
				float flicker = ((float)Math.random()*0.2f)+0.8f;
				Batch.setColor(1f, 1f, 1f, flicker*alpha);
				Batch.draw(tex, star.x, star.y, tex.getWidth()*star.z * flicker, tex.getHeight() * star.z * flicker);
			}
		}
		
		Batch.setColor( Color.WHITE );
	}
}
