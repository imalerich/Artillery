package terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.mygdx.game.Game;

public class FogOfWar 
{
	private static ShapeRenderer sr;
	
	public static void release()
	{
		if (sr != null) sr.dispose();
	}
	
	public static void init()
	{
		sr = new ShapeRenderer();
	}
	
	public static void begin(SpriteBatch Batch)
	{
		// begin alpha mask rendering
		Batch.flush();
		
		Gdx.gl.glColorMask(false, false, false, false);
		Gdx.gl.glEnable(GL20.GL_STENCIL_TEST);
		Gdx.gl.glStencilFunc(GL20.GL_ALWAYS, 1, 0xff);
		Gdx.gl.glStencilOp(GL20.GL_KEEP, GL20.GL_KEEP, GL20.GL_REPLACE);
		Gdx.gl.glStencilMask(0xFF);
		Gdx.gl.glClear(GL20.GL_STENCIL_BUFFER_BIT);
		Batch.end();
		
		sr.setProjectionMatrix(Game.getProj().combined);
		sr.begin(ShapeType.Filled);
	}
	
	public static void addVisibleRegion(float ScreenX, float ScreenY, int Radius)
	{
		sr.setColor(1, 1, 1, 1);
		sr.circle(ScreenX, ScreenY, Radius);
	}
	
	public static void end(SpriteBatch Batch)
	{
		sr.end();
		Batch.begin();
	}
	
	public static void maskOn(SpriteBatch Batch)
	{
		// turn the mask on
		Gdx.gl.glColorMask(true, true, true, true);
		Gdx.gl.glStencilMask(0x00);
		Gdx.gl.glStencilFunc(GL20.GL_EQUAL, 1, 0xFF);
	}
	
	public static void maskOff(SpriteBatch Batch)
	{
		// reset the blend function
		Batch.flush();
		Batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glDisable(GL20.GL_STENCIL_TEST);
	}
}
