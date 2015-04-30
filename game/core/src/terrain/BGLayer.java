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
	private Color day;
	private Color night;
	private boolean usenight = false;
	
	public BGLayer(String Filename, Color C, float Ratio, float XSpeed)
	{
		tex = new Texture( Gdx.files.internal(Filename) );
		ratio = Ratio;
		xspeed = XSpeed;
		day = C;
		night = C;
		
		if (fill == null) {
			Pixmap tmp = new Pixmap(Game.SCREENW, Game.WORLDH, Pixmap.Format.RGB888);
			tmp.setColor( Color.WHITE );
			tmp.fill();
			fill = new Texture(tmp);
			tmp.dispose();
		}
	}
	
	public void setNightColor(Color C)
	{
		usenight = true;
		night = C;
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
	
	public Color getColor()
	{
		return day;
	}
	
	public void setAlpha(float A)
	{
		day.a = A;
		night.a = A;
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
	
	private void setColor(SpriteBatch Batch)
	{
		Color c = new Color(day);
		
		if (usenight) {
			if (TimeOfDay.isNight())
				c = new Color(night);
			else if (TimeOfDay.isTrans()) {
				float d = TimeOfDay.getTrans();
				float n = 1f - d;
			
				c.r = c.r*d + night.r*n;
				c.g = c.g*d + night.g*n;
				c.b = c.b*d + night.b*n;
			}
		}
		
		Batch.setColor(c);
	}
	
	public void draw(SpriteBatch Batch, Camera Cam)
	{
		float ypos = Cam.getRenderY( Game.WORLDH - tex.getHeight() );
		
		setColor(Batch);
		Batch.draw(tex, xpos-tex.getWidth()*2f, ypos);
		Batch.draw(tex, xpos-tex.getWidth(), ypos);
		Batch.draw(tex, xpos, ypos);
		Batch.draw(tex, xpos+tex.getWidth(), ypos);
		Batch.draw(tex, xpos+tex.getWidth()*2f, ypos);
		
		for (int x=0; x<=(Game.SCREENW/fill.getWidth()); x++)
			Batch.draw(fill, x*fill.getWidth(), Cam.getRenderY( Game.WORLDH - tex.getHeight() - fill.getHeight()));
		
		Batch.setColor(Color.WHITE);
	}
}
