package terrain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Game;

public class Background {
	//public static final Color BGCOLOR = new Color(82/255f, 24/255f, 20/255f, 1f);
	//public static final Color FGCOLOR = new Color(99/255f, 33/255f, 28/255f, 1f);
	public static final Color BGCOLOR = new Color(44/255f, 82/255f, 129/255f, 1f);
	public static final Color FGCOLOR = new Color(66/255f, 108/255f, 160/255f, 1f);
	
	private static Texture bg;
	private static Texture fg;
	
	public static void release()
	{
		if (bg != null)
			bg.dispose();
		
		if (fg != null)
			fg.dispose();
	}
	
	public static void init()
	{
		if (bg == null) {
			Pixmap tmp = new Pixmap(Game.SCREENW, Game.SCREENH, Pixmap.Format.RGB888);
			tmp.setColor( BGCOLOR );
			tmp.fill();
			bg = new Texture(tmp);
			tmp.dispose();
		}
		
		if (fg == null) {
			Pixmap tmp = new Pixmap(Game.SCREENW, Game.SCREENH, Pixmap.Format.RGB888);
			tmp.setColor( FGCOLOR );
			tmp.fill();
			fg = new Texture(tmp);
			tmp.dispose();
		}
	}
	
	public static void drawBG(SpriteBatch Batch)
	{
		Batch.draw(bg, 0, 0, Game.SCREENW, Game.SCREENH);
	}
	
	public static void drawFG(SpriteBatch Batch)
	{
		Batch.draw(fg, 0, 0, Game.SCREENW, Game.SCREENH);
	}
}
