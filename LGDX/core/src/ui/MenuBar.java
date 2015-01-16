package ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.Camera;
import com.mygdx.game.Cursor;
import com.mygdx.game.Game;

public class MenuBar 
{
	public static TextureRegion[] CHARSET;
	private static Texture bar;
	private static Texture endbutton;
	private static Texture endinactive;
	private static Texture powerbar;
	private static Texture powerindicator;
	private static Texture reqflag;
	private static Texture reqx;
	private static TextureRegion[] currentstage;
	private static Rectangle bbox;
	
	private static float powerlevel = PowerButtons.DEFAULTPOWER/PowerButtons.MAXPOWER;
	private static int req = 0;
	private static int tmpreq = 0;
	private static boolean isUsersTurn = false;
	
	public static void init()
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
		
		if (reqflag == null)
			reqflag = new Texture( Gdx.files.internal("img/ui/menubar/reqflag.png") );
		
		if (reqx == null)
			reqx = new Texture( Gdx.files.internal("img/ui/menubar/reqx.png") );
		
		if (currentstage == null) {
			Texture tmp = new Texture( Gdx.files.internal("img/ui/menubar/CurrentStage.png") );
			currentstage = TextureRegion.split(tmp, tmp.getWidth()/5, tmp.getHeight())[0];
		}
		
		if (CHARSET == null) {
			Texture tmp = new Texture( Gdx.files.internal("img/ui/menubar/charset.png") );
			CHARSET = TextureRegion.split(tmp, tmp.getWidth()/11, tmp.getHeight())[0];
		}
		
		bbox = new Rectangle(Game.SCREENW/2 - endbutton.getWidth()/2, 
				Game.SCREENH-endbutton.getHeight()+2, endbutton.getWidth(), endbutton.getHeight());
	}
	
	public static void release()
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
		
		if (reqflag != null)
			reqflag.dispose();
		
		if (reqx != null)
			reqx.dispose();
	}
	
	public static void setPowerLevel(float Level, float Maximum)
	{
		powerlevel = Level/Maximum;
	}
	
	public static float getPowerLevel()
	{
		return powerlevel;
	}
	
	public static void setEndButttonPos()
	{
		bbox.x = Game.SCREENW/2 - endbutton.getWidth()/2;
		bbox.y = Game.SCREENH - endbutton.getHeight()+2;
	}
	
	public static int getMenuBarHeight()
	{
		return bar.getHeight();
	}
	
	public static void setRequisition(int Req)
	{
		req = Req;
	}
	
	public static void setTmpRequisition(int Req)
	{
		tmpreq = Req;
	}
	
	public static boolean isEndTurn()
	{
		return (Cursor.isMouseOverAbsolute(bbox) && Cursor.isButtonJustReleased(Cursor.LEFT));
	}
	
	public static void setUsersTurn(boolean State)
	{
		isUsersTurn = State;
	}
	
	public static void draw(SpriteBatch Batch, Camera Cam, int CurrentStage, boolean Active)
	{
		for (int x=0; x<Game.WORLDW; x += bar.getWidth())
			Batch.draw(bar, x, Game.SCREENH-bar.getHeight());
		
		int offset = 0;
		setEndButttonPos();
		if (Cursor.isMouseOverAbsolute(bbox) && Cursor.isButtonPressed(Cursor.LEFT))
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
		
		if (isUsersTurn) {
			Batch.draw(currentstage[CurrentStage], Game.SCREENW - currentstage[CurrentStage].getRegionWidth() - 2,
					Game.SCREENH-currentstage[CurrentStage].getRegionHeight()+3);
		} else {
			Batch.draw(currentstage[4], Game.SCREENW - currentstage[4].getRegionWidth() - 2,
					Game.SCREENH-currentstage[4].getRegionHeight()+3);
		}
		
		drawReq(Batch, Cam, Game.SCREENW - currentstage[CurrentStage].getRegionWidth() - 16);
	}
	
	public static void drawReq(SpriteBatch Batch, Camera Cam, int XPos)
	{
		// set the color to red to indicate cost of a unit
		if (req != tmpreq) {
			if (tmpreq < 0)
				Batch.setColor(Color.RED);
			else
				Batch.setColor(Color.GREEN);
		}
		
		int i=0;
		int val = tmpreq;
		
		boolean negative = false;
		if (val < 0) {
			negative = true;
			val = Math.abs(val);
		}
		
		while (true) {
			int digit = val % 10;
			if (val == 0 && i != 0) {
				if (negative) {
					digit = CHARSET.length-1;
					Batch.draw(CHARSET[digit], XPos - i*CHARSET[digit].getRegionWidth()-i, Game.SCREENH-6-CHARSET[digit].getRegionHeight());
					i++;
				}
				
				if (req != tmpreq && tmpreq < 0) {
					Batch.draw(reqx, XPos - (i-1)*CHARSET[0].getRegionWidth()-i-reqflag.getWidth()-4, 
							Game.SCREENH-6-CHARSET[0].getRegionHeight());
				} else {
					Batch.draw(reqflag, XPos - (i-1)*CHARSET[0].getRegionWidth()-i-reqflag.getWidth()-4, 
							Game.SCREENH-6-CHARSET[0].getRegionHeight());
				}
				
				break;
			}
			
			Batch.draw(CHARSET[digit], XPos - i*CHARSET[digit].getRegionWidth()-i, Game.SCREENH-6-CHARSET[digit].getRegionHeight());

			
			val -= digit;
			val /= 10;
			i++;
		}
		
		// reset the color
		if (req != tmpreq) {
			Batch.setColor(Color.WHITE);
		}
	}
}
