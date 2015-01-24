package particles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;

public class Weather {
	private static final Color COLOR = new Color(1f, 1f, 1f, 0.1f);
	private static final int SPEEDY = 64;
	private static final int SPEEDX = -32;
	
	private static Texture tex;
	private static float offsety = 0;
	private static float offsetx = 0;
	
	public static void init()
	{
		if (tex == null) 
			tex = new Texture( Gdx.files.internal("img/particles/snow.png") );
		
	}
	
	public static void release()
	{
		if (tex != null)
			tex.dispose();
	}
	
	public static void draw(SpriteBatch Batch, Camera cam)
	{
		Batch.setColor(COLOR);
		
	    offsety -= Gdx.graphics.getDeltaTime() * SPEEDY;
	    offsetx -= Gdx.graphics.getDeltaTime() * SPEEDX;
	    verifyOffsets();
	    
	    float camoff =  cam.getPos().x % tex.getWidth();
	    
    
	    for (int i=-2; i<=Game.SCREENW/tex.getWidth() + 3; i++) {
	        for (int j=-2; j<=Game.SCREENH/tex.getHeight() +  2; j++) {
	            Batch.draw(tex, i*tex.getWidth() + offsetx - camoff, j*tex.getHeight() + offsety);
	            
	        }
	    }
	    
	    Batch.setColor(Color.WHITE);
	}
	
	private static void verifyOffsets()
	{
	    if (offsety < -tex.getHeight())
	    	offsety += tex.getHeight();
	    
	    if (offsetx < -tex.getWidth())
	    	offsetx += tex.getWidth();
	    
	    if (offsetx > tex.getWidth())
	    	offsetx -= tex.getWidth();
	}
}
