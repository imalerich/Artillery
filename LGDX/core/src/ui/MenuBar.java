package ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.Camera;
import com.mygdx.game.Cursor;
import com.mygdx.game.Game;

public class MenuBar 
{
	private static Texture bar;
	private static Texture endbutton;
	private static Texture endinactive;
	private static Texture powerbar;
	private static Texture powerindicator;
	private static TextureRegion[] currentstage;
	private static Rectangle bbox;
	
	private static float powerlevel = PowerButtons.DEFAULTPOWER/PowerButtons.MAXPOWER;
	
	public static void Init()
	{
		if (bar == null)
			bar = new Texture( Gdx.files.internal("img/ui/menubar/menubar.png") );
		
		if (endbutton == null)
			endbutton = new Texture( Gdx.files.internal("img/ui/menubar/EndTurnButton.png") );
		
		if (endinactive == null)
			endinactive = new Texture( Gdx.files.internal("img/ui/menubar/EndInactive.png") );
		
		if (powerbar == null)
			powerbar = new Texture( Gdx.files.internal("img/ui/menubar/powerbar.png") );
		
		if (powerindicator == null)
			powerindicator = new Texture( Gdx.files.internal("img/ui/menubar/powerindicator.png") );
		
		if (currentstage == null)
		{
			Texture tmp = new Texture( Gdx.files.internal("img/ui/menubar/CurrentStage.png") );
			currentstage = TextureRegion.split(tmp, tmp.getWidth()/4, tmp.getHeight())[0];
		}
		
		bbox = new Rectangle(Game.SCREENW/2 - endbutton.getWidth()/2, 
				Game.SCREENH-endbutton.getHeight()+2, endbutton.getWidth(), endbutton.getHeight());
	}
	
	public static void Release()
	{
		if (bar != null)
			bar.dispose();
		
		if (endbutton != null)
			endbutton.dispose();
		
		if (endinactive != null)
			endinactive.dispose();
		
		if (powerbar != null)
			powerbar.dispose();
		
		if (powerindicator != null)
			powerindicator.dispose();
	}
	
	public static void SetPowerLevel(float Level, float Maximum)
	{
		powerlevel = Level/Maximum;
	}
	
	public static void SetEndButttonPos()
	{
		bbox.x = Game.SCREENW/2 - endbutton.getWidth()/2;
		bbox.y = Game.SCREENH - endbutton.getHeight()+2;
	}
	
	public static int GetMenuBarHeight()
	{
		return bar.getHeight();
	}
	
	public static boolean IsEndTurn()
	{
		return (Cursor.IsMouseOverAbsolute(bbox) && Cursor.isButtonJustReleased(Cursor.LEFT));
	}
	
	public static void Draw(SpriteBatch Batch, Camera Cam, int CurrentStage, boolean Active)
	{
		for (int x=0; x<Game.WORLDW; x += bar.getWidth())
			Batch.draw(bar, x, Game.SCREENH-bar.getHeight());
		
		int offset = 0;
		SetEndButttonPos();
		if (Cursor.IsMouseOverAbsolute(bbox) && Cursor.isButtonPressed(Cursor.LEFT))
			offset = 2;
		
		if (Active)
			Batch.draw(endbutton, bbox.x, bbox.y-offset);
		else
			Batch.draw(endinactive, bbox.x, bbox.y);
		
		Batch.draw(powerbar, 2, Game.SCREENH-powerbar.getHeight()+3);
		Batch.draw(powerindicator, 6, Game.SCREENH-powerbar.getHeight()+14, 
				(int)(powerindicator.getWidth()*powerlevel), powerindicator.getHeight(), 
				0, 0, (int)(powerindicator.getWidth()*powerlevel), 
				(int)powerindicator.getHeight(), false, false);
		
		Batch.draw(currentstage[CurrentStage], Game.SCREENW - currentstage[CurrentStage].getRegionWidth() - 2,
				Game.SCREENH-currentstage[CurrentStage].getRegionHeight()+3);
	}
}
