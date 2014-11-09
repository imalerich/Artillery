package particles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


import com.mygdx.game.Camera;
import com.mygdx.game.Game;

public class Weather {
	private static Texture tex;
	private static float offset1;
	private static float offset2;
	private static final int SPEED = 16;
	
	public static void Init(){
		
		if (tex == null) {
			tex = new Texture( Gdx.files.internal("img/particles/snow.png") );
			offset1 = 0;
			
		}
		
	}
	public static void Release(){
		if (tex != null)
			tex.dispose();
	}
	
	public static void Draw(SpriteBatch Batch, Camera cam)
	{
	    
	    offset1 -= Gdx.graphics.getDeltaTime() * SPEED;
	    offset2 =  cam.GetPos().x % tex.getWidth();


	    
	    if (offset1 < -tex.getHeight())
	    {
	        offset1 = 0;
	    }

	    
	    
	    for (int i=0; i<Game.SCREENW/tex.getWidth() +2; i++)
	    {
	        for (int j=-1; j<Game.SCREENH/tex.getHeight() + 2; j++)
	        {
	            Batch.draw(tex, i*tex.getWidth() - offset2, j*tex.getHeight() + offset1);
	            
	        }
	    }
	}
}
