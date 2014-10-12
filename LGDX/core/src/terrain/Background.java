package terrain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Game;

public class Background {
	public static final Color BGCOLOR = new Color(40/255f, 20/255f, 18/255f, 1f);
	public static final Color FGCOLOR = new Color(60/255f, 25/255f, 22/255f, 1f);
	
	private static Texture bg;
	private static Texture fg;
	
	public static void Release()
	{
		if (bg != null)
			bg.dispose();
		
		if (fg != null)
			fg.dispose();
	}
	
	public static void Init()
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
	
	public static void DrawBG(SpriteBatch Batch)
	{
		Batch.draw(bg, 0, 0, Game.SCREENW, Game.SCREENH);
	}
	
	public static void DrawFG(SpriteBatch Batch)
	{
		Batch.draw(fg, 0, 0, Game.SCREENW, Game.SCREENH);
	}
}
