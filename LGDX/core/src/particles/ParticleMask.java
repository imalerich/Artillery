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
	private static int xpos0;
	private static int xpos1;
	
	private static ShapeRenderer sr;
	
	public static void Init()
	{
		if (tex == null) {
			tex = new Texture( Gdx.files.internal("img/particles/particles.png") );
			width = tex.getWidth();
			height = tex.getHeight();
		}
		
		sr = new ShapeRenderer();
	}
	
	public static void Release()
	{
		if (tex != null)
			tex.dispose();
		
		if (sr != null) sr.dispose();
	}
	
	public static void Begin(SpriteBatch Batch)
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
		sr.setProjectionMatrix(Game.GetProj().combined);
		sr.begin(ShapeType.Filled);
	}
	
	public static void End(SpriteBatch Batch)
	{
		sr.end();
		Batch.begin();
	}
	
	public static void AddVisibleRegion(float ScreenX, float ScreenY, int Radius)
	{
		sr.setColor(1, 1, 1, 1);
		sr.circle(ScreenX, ScreenY, Radius);
	}
	
	public static void Draw(SpriteBatch Batch)
	{
		MaskOn(Batch);
		
		xpos0 = (int)(Math.random()*2);
		xpos1 = (int)(Math.random()*2);
		
		for (int i=-2; i<Game.SCREENW/width + 2; i++) {
			for (int j=-2; j<Game.SCREENH/height + 2; j++) {
				Batch.draw(tex, i*width + xpos0, j*height);
				Batch.draw(tex, i*width + xpos1, j*height-2);
			}
		}
	
		MaskOff(Batch);
	}
	
	private static void MaskOn(SpriteBatch Batch)
	{
		// turn the mask on
		Gdx.gl.glColorMask(true, true, true, true);
		Gdx.gl.glStencilMask(0x00);
		Gdx.gl.glStencilFunc(GL20.GL_EQUAL,  1, 0xFF);
	}
	
	private static void MaskOff(SpriteBatch Batch)
	{
		// reset the blend function
		Batch.flush();
		Batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glDisable(GL20.GL_STENCIL_TEST);
	}
}
