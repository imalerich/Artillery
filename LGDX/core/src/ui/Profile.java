package ui;

import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Cursor;
import com.mygdx.game.MilitaryBase;

import config.ConfigSettings;
import config.SquadConfigurations;
import entity.Classification;
import entity.Gunman;
import entity.Squad;

public class Profile 
{
	public static Texture BG;
	private static Texture close;
	
	private static Rectangle closerect;
	private static Rectangle addrect;
	
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
	
	private static int getUnitCost(Squad S)
	{
		ConfigSettings c = null;
		switch (S.getClassification())
		{
		case GUNMAN:
			c = SquadConfigurations.getConfiguration(SquadConfigurations.GUNMAN);
			return 2 * (c.reqcost/c.count);
			
		case STEALTHOPS:
			c = SquadConfigurations.getConfiguration(SquadConfigurations.STEALTHOPS);
			return 2 * (c.reqcost/c.count);
			
		case SPECOPS:
			c = SquadConfigurations.getConfiguration(SquadConfigurations.SPECOPS);
			return 2 * (c.reqcost/c.count);
			
		case TOWER:
			break;
			
		case TANK:
			break;
		}
		
		return 0;
	}
	
	private static void spawnUnit(Squad S)
	{
		if (S.getArmy().getReq() < getUnitCost(S)) {
			return;
		} else {
			S.getArmy().spendRequisition(getUnitCost(S), new Vector2(S.getBBox().x + S.getBBox().width/2f, 
					S.getBBox().y + S.getBBox().height));
		}
	
		Terrain ter = S.getArmy().getWorld().getTerrain();
		Vector2 pos = new Vector2(S.getBBox().x + S.getBBox().width + Squad.SPACING - 8, 0f);
		
		// get the appropriate configuration settings
		ConfigSettings c = null;
		switch (S.getClassification())
		{
		case GUNMAN:
			c = SquadConfigurations.getConfiguration(SquadConfigurations.GUNMAN);
			S.addUnit( new Gunman(Gunman.GUNMAN, ter, pos, c.speed, c.health, c.reqbonus));
			break;
			
		case STEALTHOPS:
			c = SquadConfigurations.getConfiguration(SquadConfigurations.STEALTHOPS);
			S.addUnit( new Gunman(Gunman.STEALTHTROOPS, ter, pos, c.speed, c.health, c.reqbonus));
			break;
			
		case SPECOPS:
			c = SquadConfigurations.getConfiguration(SquadConfigurations.SPECOPS);
			S.addUnit( new Gunman(Gunman.SPECOPS, ter, pos, c.speed, c.health, c.reqbonus));
			break;
			
		case TOWER:
			break;
			
		case TANK:
			break;
		}
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
		
		S.drawMugshots(Batch, 12, ypos+(BG.getHeight()-36));
		
		int offset = (32+Squad.SPACING)*S.getUnitCount();
		addrect = new Rectangle(14+offset, ypos+BG.getHeight()-36, ProfileWeapon.add_options.getWidth(), ProfileWeapon.add_options.getHeight());
		
		offsety = 0;
		if (Cursor.isMouseOverAbsolute(addrect) && Cursor.isButtonPressed(Cursor.LEFT)) {
			offsety = 2;
		}
		
		if (Cursor.isMouseOverAbsolute(addrect) && Cursor.isButtonJustReleased(Cursor.LEFT) && 
				S.getUnitCount() < 6) {
			spawnUnit(S);
		}
		
		if (Cursor.isMouseOverAbsolute(addrect)) {
			MenuBar.setTmpRequisition(S.getArmy().getReq() - getUnitCost(S));
		}
		
		if (S.getUnitCount() < 6 && S.getClassification() != Classification.TANK &&
				S.getClassification() != Classification.TOWER) {
			Batch.draw(ProfileWeapon.add_options_bg, addrect.x, addrect.y);
			Batch.draw(ProfileWeapon.add_options, addrect.x, addrect.y-offsety);
		}
	}
}
