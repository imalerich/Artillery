package terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;

public class BGLayer 
{
	private final Texture tex;
	private static Texture fill;
	private final float ratio;
	private float xpos = 0f;
	private float xspeed;
	private Color col;
	
	public BGLayer(String Filename, Color C, float Ratio, float XSpeed)
	{
		tex = new Texture( Gdx.files.internal(Filename) );
		ratio = Ratio;
		xspeed = XSpeed;
		col = C;
		
		if (fill == null) {
			Pixmap tmp = new Pixmap(Game.SCREENW, Game.WORLDH, Pixmap.Format.RGB888);
			tmp.setColor( Color.WHITE );
			tmp.fill();
			fill = new Texture(tmp);
			tmp.dispose();
		}
	}
	
	public void release()
	{
		tex.dispose();
		
		if (fill != null)
			fill.dispose();
	}
	
	public float getWidth()
	{
		return tex.getWidth();
	}
	
	public float getHeight()
	{
		return tex.getHeight();
	}
	
	public float getPos()
	{
		return xpos;
	}
	
	public void update( Camera Cam)
	{
		xpos -= Cam.getXDistMoved() * ratio;
		xpos += xspeed * Gdx.graphics.getDeltaTime();
		if (xpos > tex.getWidth()) 
			xpos -= tex.getWidth();
		if (xpos < 0f)
			xpos += tex.getWidth();
	}
	
	public void draw(SpriteBatch Batch, Camera Cam)
	{
		float ypos = Cam.getRenderY( Game.WORLDH - tex.getHeight() );
		
		Batch.setColor(col);
		Batch.draw(tex, xpos-tex.getWidth()*2f, ypos);
		Batch.draw(tex, xpos-tex.getWidth(), ypos);
		Batch.draw(tex, xpos, ypos);
		Batch.draw(tex, xpos+tex.getWidth(), ypos);
		Batch.draw(tex, xpos+tex.getWidth()*2f, ypos);
		
		for (int x=0; x<=(Game.SCREENW/fill.getWidth()); x++)
			Batch.draw(fill, x*fill.getWidth(), Cam.getRenderY( Game.WORLDH - tex.getHeight() - fill.getHeight() ));
		
		Batch.setColor(Color.WHITE);
	}
}
