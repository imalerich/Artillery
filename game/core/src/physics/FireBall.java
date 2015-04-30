package physics;

import particles.Particles;
import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Game;

public class FireBall 
{
	private static final int PPS = 40000;
	private static final float MAXVEL = 48;
	private static final float HALFWIDTH = 128;
	
	private final Terrain ter;
	private final Particles p;
	private Vector2 pos;
	private Vector2 prev;
	
	private float dst = 0f;
	private float speed = 600f;
	
	private float time = 0f;
	
	public FireBall(Terrain Ter, Particles P, float Pos)
	{
		p = P;
		ter = Ter;
		pos = new Vector2(Pos, Pos);
		prev = new Vector2(Pos, Pos);
	}
	
	public void update()
	{
		prev.x = pos.x;
		prev.y = pos.y;
		
		pos.x += Gdx.graphics.getDeltaTime() * speed;
		pos.y -= Gdx.graphics.getDeltaTime() * speed;
		dst += Gdx.graphics.getDeltaTime() * speed;
		
		validatePos();
		addParticles();
	}
	
	public boolean isCompleted()
	{
		return dst >= HALFWIDTH;
	}
	
	private void validatePos()
	{
		if (pos.x < 0f) pos.x += Game.WORLDW;
		if (pos.y < 0f) pos.y += Game.WORLDW;
		
		if (pos.x >= Game.WORLDW) pos.x -= Game.WORLDW;
		if (pos.y >= Game.WORLDW) pos.y -= Game.WORLDW;
	}
	
	private void addParticles()
	{
		time += Gdx.graphics.getDeltaTime();

		float perc = 1f + -Math.min(dst/HALFWIDTH, 1f);
		int addcount = (int)(perc*PPS*time);
		if (addcount == 0) {
			return;
		}
		
		float maxvel = MAXVEL * perc;
		Vector2 v = new Vector2();
		float dst = time*speed;
		float scale = 0f;
		float x = 0f;
		float y = 0f;
		
		for (int i=0; i<addcount; i++) {
			scale = (float)Math.random()/2f + 0.2f;
			x = pos.x - (float)Math.random()*dst;
			y = Game.WORLDH - ter.getHeight((int)x);
			v.y = (float)Math.random() * maxvel;
			p.addEmber(scale, new Vector2(x, y), v, (float)Math.random()*5f, 0.15f);
			
			x = pos.y + (float)Math.random()*dst;
			y = Game.WORLDH - ter.getHeight((int)x);
			v.y = (float)Math.random() * maxvel;
			p.addEmber(scale, new Vector2(x, y), v, (float)Math.random()*5f, 0.15f);
		}
		
		time = 0f;
	}
}
