package particles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.mygdx.game.Game;

public class ParticleMask 
{
	private static Texture tex;
	private static int width;
	private static int height;
	
	private static ShapeRenderer sr;
	
	public static void init()
	{
		if (tex == null) {
			tex = new Texture( Gdx.files.internal("img/particles/particles.png") );
			width = tex.getWidth();
			height = tex.getHeight();
		}
		
		sr = new ShapeRenderer();
	}
	
	public static void release()
	{
		if (tex != null)
			tex.dispose();
		
		if (sr != null) sr.dispose();
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
	
	public static void end(SpriteBatch Batch)
	{
		sr.end();
		Batch.begin();
	}
	
	public static void addVisibleRegion(float ScreenX, float ScreenY, int Radius)
	{
		sr.setColor(1, 1, 1, 1);
		sr.circle(ScreenX, ScreenY, Radius);
	}
	
	public static void draw(SpriteBatch Batch)
	{
		maskOn(Batch);
		
		for (int i=-2; i<Game.SCREENW/width + 2; i++) {
			for (int j=-2; j<Game.SCREENH/height + 2; j++) {
				Batch.draw(tex, i*width, j*height);
				Batch.draw(tex, i*width, j*height-2);
			}
		}
	
		maskOff(Batch);
	}
	
	private static void maskOn(SpriteBatch Batch)
	{
		// turn the mask on
		Gdx.gl.glColorMask(true, true, true, true);
		Gdx.gl.glStencilMask(0x00);
		Gdx.gl.glStencilFunc(GL20.GL_EQUAL,  1, 0xFF);
	}
	
	private static void maskOff(SpriteBatch Batch)
	{
		// reset the blend function
		Batch.flush();
		Batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glDisable(GL20.GL_STENCIL_TEST);
	}
}
