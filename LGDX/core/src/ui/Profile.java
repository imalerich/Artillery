package ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.Cursor;
import com.mygdx.game.MilitaryBase;

import entity.Squad;

public class Profile 
{
	private static Texture bg;
	private static Texture close;
	private static Rectangle closerect;
	
	private static final int STAMPX = 212;
	private static final int STAMPY = 30;
	private static int xpos = 0;
	
	public static void Release()
	{
		if (bg != null)
			bg.dispose();
		
		if (close != null)
			close.dispose();
	}
	
	public static void Init()
	{
		if (bg == null)
			bg = new Texture( Gdx.files.internal("img/profile.png"));
		if (close == null)
			close = new Texture( Gdx.files.internal("img/closeprofile.png"));
		
		ResetPos();
	}
	
	private static void CalcCloseRect()
	{
		closerect.x = xpos+bg.getWidth()+4;
	}
	
	public static void ResetPos()
	{
		xpos = -bg.getWidth()-(close.getWidth()-4);
		
		closerect = new Rectangle(xpos+bg.getWidth()+4, 4+bg.getHeight()-close.getHeight(), 
				close.getWidth(), close.getWidth());
	}
	
	public static boolean IsMouseOverClose()
	{
		CalcCloseRect();
		return Cursor.IsMouseOverAbsolute(closerect);
	}
	
	public static void Draw(SpriteBatch Batch, Squad S, int ArmyIndex)
	{
		if (xpos < 4)
			xpos += 4*Gdx.graphics.getDeltaTime()*(bg.getWidth()+4);
		if (xpos > 4)
			xpos = 4;
		
		int offsety = 0;
		if (IsMouseOverClose() && Cursor.isButtonPressed(Cursor.LEFT))
			offsety = 2;
		
		Batch.draw(bg, xpos, 4);
		Batch.draw(MilitaryBase.GetLogo(ArmyIndex), xpos+STAMPX, 4+(bg.getHeight()-STAMPY));
		Batch.draw(close, closerect.x, closerect.y-offsety);
	}
}
