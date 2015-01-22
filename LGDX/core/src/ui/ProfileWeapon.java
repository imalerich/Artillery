package ui;

import arsenal.Armament;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Cursor;

import entity.Squad;

public class ProfileWeapon 
{
	// TODO 
	public static int UPGRADE_COST = 100;
	
	private static Texture stamp;
	private static Texture specs;
	private static Texture seperator;
	private static Texture settings_bar;
	private static Texture add_options;
	private static Texture add_options_bg;
	
	private static int RIFLE = 0;
	private static int MISSILE = 1;
	private static int GRENADE = 2;
	private static int WEAPONCOUNT = 3;
	private static TextureRegion[] weapons;
	
	private static Rectangle add_opt_rect;
	
	public static void init()
	{
		if (stamp == null)
			stamp = new Texture( Gdx.files.internal("img/ui/profile/stamp.png") );
		
		if (specs == null)
			specs = new Texture( Gdx.files.internal("img/ui/profile/specs.png") );
		
		if (settings_bar == null)
			settings_bar = new Texture( Gdx.files.internal("img/ui/profile/settings_bar.png") );
		
		if (seperator == null)
			seperator = new Texture( Gdx.files.internal("img/ui/profile/seperator.png") );
		
		if (add_options == null)
			add_options = new Texture( Gdx.files.internal("img/ui/profile/add_options.png") );
		
		if (add_options_bg == null)
			add_options_bg = new Texture( Gdx.files.internal("img/ui/profile/add_options_bg.png") );
		
		if (weapons == null)
		{
			Texture tmp = new Texture( Gdx.files.internal("img/ui/profile/weapon_icons.png") );
			weapons = TextureRegion.split(tmp, tmp.getWidth()/WEAPONCOUNT, tmp.getHeight())[0];
		}
		
		add_opt_rect = new Rectangle(Profile.BG.getWidth() - add_options.getWidth() - 5, -add_options.getHeight()-2, 
				add_options.getWidth(), add_options.getHeight());
	}
	
	public static void release()
	{
		if (stamp != null)
			stamp.dispose();
		
		if (settings_bar != null)
			settings_bar.dispose();
		
		if (specs != null)
			specs.dispose();
		
		if (seperator != null)
			seperator.dispose();
		
		if (add_options != null)
			add_options.dispose();
		
		if (add_options_bg != null)
			add_options_bg.dispose();
	}
	
	public static int getHeight()
	{
		return stamp.getHeight() + 5;
	}
	
	public static void draw(SpriteBatch Batch, Squad S, Armament A, boolean Primary, int XPos, int YPos)
	{
		if (A == null)
			return;
		
		// draw the stamp and seperators
		Batch.draw(seperator, XPos, YPos);
		Batch.draw(stamp, XPos+5, YPos-stamp.getHeight()-2);
		Batch.draw(seperator, XPos, YPos - stamp.getHeight()-5);
		
		// draw the weapons specs
		Batch.draw(specs, XPos+stamp.getWidth()+8, YPos-stamp.getHeight()-2);
		
		float str = Math.min( A.getStrength()/20f, 1f );
		if (A.getType() == Armament.POINTTARGET) {
			str = Math.min( A.getStrength()/40f, 1f );
		}
		
		float range = Math.min( A.getRange()/1024f, 1f ) ;
		float shots = Math.min( A.getFireRate()/3f, 1f );
		float acc = Math.min( (A.getAccuracy() - 0.5f) * 2f, 1f );
		
		Batch.setColor(.298f, 0.294f, 0.3647f, 1f);
		Batch.draw(settings_bar, XPos+stamp.getWidth()+33, YPos-stamp.getHeight()-2 + 21,
				(int)(settings_bar.getWidth()*str), (int)settings_bar.getHeight(), 0, 0, 
				(int)(settings_bar.getWidth()*str), (int)settings_bar.getHeight(), false, false);
		
		Batch.draw(settings_bar, XPos+stamp.getWidth()+33, YPos-stamp.getHeight()-2 + 21 - 6,
				(int)(settings_bar.getWidth()*range), (int)settings_bar.getHeight(), 0, 0, 
				(int)(settings_bar.getWidth()*range), (int)settings_bar.getHeight(), false, false);
		
		Batch.draw(settings_bar, XPos+stamp.getWidth()+33, YPos-stamp.getHeight()-2 + 21 - 12,
				(int)(settings_bar.getWidth()*shots), (int)settings_bar.getHeight(), 0, 0, 
				(int)(settings_bar.getWidth()*shots), (int)settings_bar.getHeight(), false, false);
				
		Batch.draw(settings_bar, XPos+stamp.getWidth()+33, YPos-stamp.getHeight()-2 + 21 - 18, 
				(int)(settings_bar.getWidth()*acc), (int)settings_bar.getHeight(), 0, 0, 
				(int)(settings_bar.getWidth()*acc), (int)settings_bar.getHeight(), false, false);
		
		Batch.setColor(0.8588f, 0.788235284f, 0.6941176f, 1f);
		if (A.getType() == Armament.POINTTARGET) {
			Batch.draw(settings_bar, XPos+stamp.getWidth()+33, YPos-stamp.getHeight()-2 + 21 - 6);
			Batch.draw(settings_bar, XPos+stamp.getWidth()+33, YPos-stamp.getHeight()-2 + 21 - 18);
		}
		
		Batch.setColor(Color.WHITE);
		
		int index = 0;
		if (A.getType() == Armament.POINTTARGET) {
			if (Primary)
				index = MISSILE;
			else
				index= GRENADE;
		} else {
			index = RIFLE;
		}
		
		// draw the weapon stamp overlay
		Batch.draw(weapons[index], XPos+5, YPos-stamp.getHeight()-2);
		
		// draw the options button
		int yoff = 0;
		Rectangle r = new Rectangle(add_opt_rect.x + XPos, add_opt_rect.y + YPos, add_opt_rect.width, add_opt_rect.height);
		if (Cursor.isMouseOverAbsolute(r) && Cursor.isButtonPressed(Cursor.LEFT)) {
			yoff = -2;
		}
		
		if (Cursor.isMouseOverAbsolute(r) && !A.isMaxed())
			MenuBar.setTmpRequisition( S.getArmy().getReq() - UPGRADE_COST );
		
		if (Cursor.isMouseOverAbsolute(r) && Cursor.isButtonJustReleased(Cursor.LEFT) &&
				S.getArmy().getReq() >= UPGRADE_COST && !A.isMaxed()) {
			S.getArmy().spendRequisition(UPGRADE_COST, 
					new Vector2(S.getBBox().x + S.getBBox().width/2f, S.getBBox().y + S.getBBox().height));
			
			A.addFireRate(0.2f);
			A.addRange(32);
			A.addAccuracy(0.02f);
			A.addStrength(1);
		}
		
		Batch.draw(add_options_bg, r.x, r.y);
		Batch.draw(add_options, r.x, r.y + yoff);
	}
}
