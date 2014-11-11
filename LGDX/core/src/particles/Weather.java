package particles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;

public class Weather {
	private static final Color COLOR = new Color(1f, 1f, 1f, 0.2f);
	private static final int SPEEDY = 32;
	private static final int SPEEDX = 16;
	
	private static Texture tex;
	private static float offsety = 0;
	private static float offsetx = 0;
	
	public static void Init()
	{
		if (tex == null) 
			tex = new Texture( Gdx.files.internal("img/particles/snow.png") );
		
	}
	
	public static void Release()
	{
		if (tex != null)
			tex.dispose();
	}
	
	public static void Draw(SpriteBatch Batch, Camera cam)
	{
		Batch.setColor(COLOR);
		
	    offsety -= Gdx.graphics.getDeltaTime() * SPEEDY;
	    offsetx -= Gdx.graphics.getDeltaTime() * SPEEDX;
	    VerifyOffsets();
	    
	    float camoff =  cam.GetPos().x % tex.getWidth();
	    
    
	    for (int i=0; i<Game.SCREENW/tex.getWidth() +2; i++) {
	        for (int j=-1; j<Game.SCREENH/tex.getHeight() + 2; j++) {
	            Batch.draw(tex, i*tex.getWidth() + offsetx - camoff, j*tex.getHeight() + offsety);
	            
	        }
	    }
	    
	    Batch.setColor(Color.WHITE);
	}
	
	private static void VerifyOffsets()
	{
	    if (offsety < -tex.getHeight())
	    	offsety += tex.getHeight();
	    
	    if (offsetx < -tex.getWidth())
	    	offsetx += tex.getWidth();
	}
}
