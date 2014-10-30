package physics;

import terrain.Terrain;
import arsenal.Armament;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;

import entity.Unit;

public class CombatPacket 
{
	public static final Color BULLETCOL = new Color(128/255f, 128/255f, 128/255f, 1f);
	public static final int BULLETDIMMENSIONX = 3;
	public static final int BULLETDIMMENSIONY = 2;
	public static final float HALFWIDTH = BULLETDIMMENSIONX/2f;
	private static Texture tex;
	
	private final Unit offense;
	private final Unit defense;
	private final Armament arms;
	private boolean iscompleted;
	
	private Vector2 pos;
	private Vector2 target;
	private Vector2 speed;
	
	private Terrain ter;
	private Rectangle targetBBox;
	
	public static void Init()
	{
		if (tex == null)
		{
			Pixmap tmp = new Pixmap(BULLETDIMMENSIONX, BULLETDIMMENSIONY, Pixmap.Format.RGB888);
			tmp.setColor(BULLETCOL);
			tmp.fill();
			tex = new Texture(tmp);
			tmp.dispose();
		}
	}
	
	public static void Release()
	{
		if (tex != null)
			tex.dispose();
	}
	
	public CombatPacket(Terrain Ter, Unit Offense, Unit Defense, Armament Arms)
	{
		ter = Ter;
		offense = Offense;
		defense = Defense;
		arms = Arms;
		iscompleted = false;
		
		// set the initial position and the target position
		pos = new Vector2(offense.GetPos().x + offense.GetWidth()/2f, 
				offense.GetPos().y + offense.GetHeight()/2f);
		target = new Vector2(defense.GetPos().x, defense.GetPos().y + defense.GetHeight()/2f);
		
		// calculate the speed in each direction
		speed = new Vector2(target);
		speed.x -= pos.x;
		speed.y -= pos.y;
		speed.nor();
		speed.x = Math.abs(speed.x * arms.GetSpeed());
		speed.y *= arms.GetSpeed();
		
		// set the target bounding box
		targetBBox = new Rectangle(target.x, target.y, defense.GetWidth(), defense.GetHeight());
	}
	
	public void Update()
	{
		// check if this unit has reached his position
		if (targetBBox.contains(pos.x + HALFWIDTH, 
				pos.y + HALFWIDTH)) {
			iscompleted = true;
			return;
		}
		
		if (ter.Contains(pos.x, pos.y)) {
			iscompleted = true;
			return;
		}
			
		int direction = GetMoveDirection();
		pos.x += (direction * speed.x * Gdx.graphics.getDeltaTime());
		pos.y += (speed.y * Gdx.graphics.getDeltaTime());
		
		if (pos.x < 0) {
			pos.x += Game.WORLDW;
		} else if (pos.x > Game.WORLDW) {
			pos.x -= Game.WORLDW;
		}
		
		if (direction == 0) {
			iscompleted = true;
			return;
		}
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam)
	{
		Batch.draw(tex, Cam.GetRenderX(pos.x), Cam.GetRenderY(pos.y));
	}
	
	public Unit GetOffense()
	{
		return offense;
	}
	
	public Unit GetDefense()
	{
		return defense;
	}
	
	public Vector2 GetPosition()
	{
		return pos;
	}
	
	public Armament GetArmament()
	{
		return arms;
	}
	
	public void SetCompleted()
	{
		iscompleted = true;
	}
	
	public boolean IsCompleted()
	{
		return iscompleted;
	}
	
	private int GetMoveDirection()
	{
		int width = BULLETDIMMENSIONX;


		// check the distance to the target in each direction
		float rdist = (Game.WORLDW-(pos.x+width)) + target.x;
		if (target.x > pos.x)
			rdist = target.x -(pos.x);

		float ldist = pos.x + (Game.WORLDW - target.x);
		if (target.x < pos.x)
			ldist = (pos.x - target.x);
		
		if (rdist < ldist) {
			return 1;
		} else if (ldist < rdist) {
			return -1;
		} else {
			return 0;
		}
	}
}
