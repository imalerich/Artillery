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
	public static Texture BG;
	private static Texture close;
	private static Rectangle closerect;
	
	private static final int STAMPX = 212;
	private static final int STAMPY = 30;
	private static int ypos = 0;
	
	public static void release()
	{
		if (BG != null)
			BG.dispose();
		
		if (close != null)
			close.dispose();
	}
	
	public static void init()
	{
		if (BG == null)
			BG = new Texture( Gdx.files.internal("img/ui/profile/profile.png") );
		
		if (close == null)
			close = new Texture( Gdx.files.internal("img/ui/profile/closeprofile.png") );
		
		resetPos();
	}
	
	private static void calcCloseRect()
	{
		closerect.y = ypos;
	}
	
	public static void resetPos()
	{
		ypos = -BG.getHeight()-4;
		
		closerect = new Rectangle(4, 0, close.getWidth(), close.getHeight());
	}
	
	public static boolean isMouseOverClose()
	{
		calcCloseRect();
		return Cursor.isMouseOverAbsolute(closerect);
	}
	
	public static void draw(SpriteBatch Batch, Squad S, int ArmyIndex)
	{
		if (ypos < 4)
			ypos += 4*Gdx.graphics.getDeltaTime()*(BG.getHeight()+4);
		if (ypos > 4)
			ypos = 4;
		
		int offsety = 0;
		if (isMouseOverClose() && Cursor.isButtonPressed(Cursor.LEFT))
			offsety = 2;
		
		Batch.draw(BG, 4, ypos);
		Batch.draw(MilitaryBase.getLogo(ArmyIndex), 4+STAMPX, ypos+(BG.getHeight()-STAMPY));
		Batch.draw(close, closerect.x, closerect.y-offsety);
		
		MenuBar.setTmpRequisition( S.getArmy().getReq() );
		ProfileWeapon.draw(Batch, S, S.getPrimary(), true, 4, ypos+BG.getHeight()-57);
		ProfileWeapon.draw(Batch, S, S.getSecondary(), false, 4, ypos+BG.getHeight()-57 - ProfileWeapon.getHeight());
		
		S.drawMugshots(Batch, 10, ypos+(BG.getHeight()-36));
	}
}
