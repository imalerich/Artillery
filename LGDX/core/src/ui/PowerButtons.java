package ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Cursor;

public class PowerButtons 
{
	public static final int MINPOWER = 20;
	public static final int MAXPOWER = 100;
	public static final float DEFAULTPOWER = 75;
	
	private static final float DELAY = 0.5f;
	private static final int SPEED = 22;
	private static Texture tex0;
	private static Texture tex1;
	private static Texture tex2;
	
	private Rectangle r0;
	private Rectangle r1;
	private Rectangle r2;
	
	private float power;
	private int mouseover;
	private float timer;
	
	public static void init()
	{
		if (tex0 == null) {
			tex0 = new Texture( Gdx.files.internal("img/ui/indicators/modpowerup.png") );
		}
		
		if (tex1 == null) {
			tex1 = new Texture( Gdx.files.internal("img/ui/indicators/modfire.png") );
		}
		
		if (tex2 == null) {
			tex2 = new Texture( Gdx.files.internal("img/ui/indicators/modpowerdown.png") );
		}
	}
	
	public static void release()
	{
		if (tex0 != null) {
			tex0.dispose();
		}
		
		if (tex1 != null) {
			tex1.dispose();
		}
		
		if (tex2 != null) {
			tex2.dispose();
		}
	}
	
	public PowerButtons()
	{
		r0 = new Rectangle();
		r1 = new Rectangle();
		r2 = new Rectangle();
		
		power = DEFAULTPOWER; // 0 -> 100
		mouseover = 0;
		timer = 0f;
	}
	
	public void setPos(Vector2 Pos)
	{
		r0 = new Rectangle(Pos.x - tex0.getWidth()/2f, Pos.y+tex1.getHeight()/2f, 
				tex0.getWidth(), tex0.getHeight());
		r1 = new Rectangle(Pos.x - tex1.getWidth()/2f, Pos.y-tex1.getHeight()/2f, tex1.getWidth(), tex1.getHeight());
		r2 = new Rectangle(Pos.x - tex2.getWidth()/2f, Pos.y-tex1.getHeight()/2f -tex2.getHeight(), 
				tex2.getWidth(), tex2.getHeight());
	}
	
	public void setPos(float X, float Y)
	{
		setPos(new Vector2(X, Y));
	}
	
	public float getPower()
	{
		return power;
	}
	
	public int getMouseOver(Camera Cam)
	{
		if (Cursor.isMouseOver(r0, Cam.getPos())) {
			return 1;
		} else if (Cursor.isMouseOver(r2, Cam.getPos())) {
			return -1;
		}
		
		return 0;
	}
	
	public void update(Camera Cam)
	{
		if (Cursor.isButtonPressed(Cursor.LEFT)) {
			// if this is a fresh press
			int over = getMouseOver(Cam);
			if (over != mouseover) {
				power += 2*over;
				assertPower();
				mouseover = over;
				timer = 0f;
				return;
			}
			
			// otherwise go on a delay
			timer += Gdx.graphics.getDeltaTime();
			if (timer < DELAY) {
				mouseover = over;
				return;
			}
			
			mouseover = over;
			power += over * Gdx.graphics.getDeltaTime() * SPEED;
			assertPower();
		} else {
			mouseover = 0;
			timer = 0f;
		}
		
		power -= Gdx.graphics.getDeltaTime() * SPEED * Cursor.getScrollDirection() * 4;
		assertPower();
	}
	
	public boolean doFire(Camera Cam)
	{
		if (Cursor.isMouseOver(r1, Cam.getPos()) && Cursor.isButtonJustReleased(Cursor.LEFT)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void draw(SpriteBatch Batch, Camera Cam)
	{
		int offset0 = 0;
		int offset1 = 0;
		int offset2 = 0;
		
		if (Cursor.isButtonPressed(Cursor.LEFT))
		{
			if (Cursor.isMouseOver(r0, Cam.getPos())) {
				offset0 = -4;
			} else if (Cursor.isMouseOver(r1, Cam.getPos())) {
				offset1 = -4;
			} else if (Cursor.isMouseOver(r2, Cam.getPos())) {
				offset2 = -4;
			}
		}
		
		// draw the pointer textures
		Batch.draw(tex0, Cam.getRenderX(r0.x), Cam.getRenderY(r0.y + offset0));
		Batch.draw(tex1, Cam.getRenderX(r1.x), Cam.getRenderY(r1.y + offset1));
		Batch.draw(tex2, Cam.getRenderX(r2.x), Cam.getRenderY(r2.y + offset2));
	}
	
	private void assertPower()
	{
		if (power > MAXPOWER) {
			power = MAXPOWER;
		} else if (power < MINPOWER) {
			power = MINPOWER;
		}
	}
}
