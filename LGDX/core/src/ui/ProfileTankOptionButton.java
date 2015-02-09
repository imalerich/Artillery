package ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.Cursor;

public class ProfileTankOptionButton 
{
	private static Texture tank_options_bg;
	private static Texture tank_options_fg;
	private static TextureRegion[] tank_options;
	
	private static final int TANKOPTIONS = 2;
	
	private static Rectangle r;
	
	public static void release()
	{
		if (tank_options_bg != null)
			tank_options_bg.dispose();
		
		if (tank_options_fg != null)
			tank_options_fg.dispose();
	}
	
	public static void init()
	{
		if (tank_options_bg == null)
			tank_options_bg = new Texture( Gdx.files.internal("img/ui/profile/tank_options_bg.png"));
		
		if (tank_options_fg == null)
			tank_options_fg = new Texture( Gdx.files.internal("img/ui/profile/tank_options_fg.png"));
		
		if (tank_options == null)
		{
			Texture tmp = new Texture( Gdx.files.internal("img/ui/profile/tank_options.png") );
			tank_options = TextureRegion.split(tmp, tmp.getWidth()/TANKOPTIONS, tmp.getHeight())[0];
		}
		
		r = new Rectangle(0, 0, tank_options_bg.getWidth(), tank_options_bg.getHeight());
	}
	
	public static int getWidth()
	{
		return tank_options_bg.getWidth();
	}
	
	public static void setPos(int XPos, int YPos, int OffsetX, int OffsetY)
	{
		r.x = XPos + 4+OffsetX;
		r.y = YPos + Profile.BG.getHeight()-57 - OffsetY - tank_options_bg.getHeight() - 2;
	}
	
	public static int getOffset()
	{
		if (Cursor.isMouseOverAbsolute(r) && Cursor.isButtonPressed(Cursor.LEFT))
			return 2;
		else
			return 0;
	}
	
	public static boolean isActive()
	{
		return ( Cursor.isMouseOverAbsolute(r) && Cursor.isButtonJustReleased(Cursor.LEFT) );
	}
	
	public static void draw(SpriteBatch Batch, boolean DrawFG, int Index)
	{
		Batch.draw(tank_options_bg, r.x, r.y);
		if (DrawFG)
			Batch.draw(tank_options_fg, r.x, r.y - getOffset());
		Batch.draw(tank_options[Index], r.x, r.y - getOffset());
	}
}
