package physics;

import particles.Particle;
import particles.Particles;
import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;
import com.mygdx.game.Shaders;

import entity.Tank;

public class NullTank 
{
	private static final int PPS = 8; // particles per second
	
	private Texture tex;
	private TextureRegion tr;
	private Vector2 barrelOffset;
	private Vector2 pos;
	
	protected float barrelPhi = 0.0f;
	private int halfwidth;
	private int barrelwidth;
	private int barrelheight;
	
	private int width;
	private int height;
	private boolean forward;
	private Terrain ter;
	private Particles particles;
	
	private float time;
	
	public NullTank(Particles Part, Terrain Ter, Texture Tex, Vector2 BarrelOffset, Vector2 Pos, 
			float BarrelPhi, boolean Forward) {
		particles = Part;
		ter = Ter;
		tex = Tex;
		tr = new TextureRegion(tex);
		pos = Pos;
		barrelOffset = BarrelOffset;
		
		forward = Forward;
		width = tex.getWidth();
		height = tex.getHeight();
		
		barrelPhi = BarrelPhi;
		halfwidth = tex.getWidth()/2;
		barrelwidth = Tank.BARREL.getWidth();
		barrelheight = Tank.BARREL.getHeight();
		
		time = 0f;
	}
	
	public void Update(Camera Cam)
	{
		time += Gdx.graphics.getDeltaTime();
		
		int addcount = (int)(PPS*time*2);
		if (addcount == 0) {
			return;
		}
		
		for (int i=0; i<addcount; i++) {
			float Radius = (float)(Math.random()*width/2);
			
			float yoff = Particle.GRAVITY * (1/(float)addcount) * (float)time;
			particles.AddParticle(Radius, new Vector2(pos.x + width/2, pos.y+yoff+height/2));
		}
		
		time = 0f;
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam)
	{
		Shaders.SetShader(Batch, Shaders.nulltank);
		
		RenderBarrel(Batch, Cam);
		Render(Batch, Cam);
		
		Shaders.RevertShader(Batch);
	}
	
	private void Render(SpriteBatch Batch, Camera Cam)
	{
		float theta = GetAngle();
		
		// draw the tank
		tr.setRegion(0, 0, width, height);
		tr.flip(!forward, false);
		
		Batch.draw(tr, Cam.GetRenderX(pos.x), 
				Cam.GetRenderY(pos.y), halfwidth, 0, width, height, 1f, 1f, theta);
	}
	
	private void RenderBarrel(SpriteBatch Batch, Camera Cam)
	{
		float theta = GetAngle();
		float phi = barrelPhi;
		if (!forward)
			phi = 180 - phi;
		
		// draw the tanks barrel
		Vector2 offset = new Vector2(barrelOffset.x-halfwidth, barrelOffset.y);
		if (!forward) offset.x = halfwidth - barrelOffset.x;
		offset.rotate((float)Math.toRadians(theta));
		
		// draw the tanks barrel
		Batch.draw(Tank.BARREL, Cam.GetRenderX(pos.x + halfwidth + offset.x),
				Cam.GetRenderY(pos.y + offset.y),
				0, barrelheight/2f, barrelwidth, barrelheight, 1f, 1f, 
				phi + theta, 0, 0, barrelwidth, barrelheight, false, false);
	}
	
	private float GetAngle()
	{
		float theta = 0.0f;
		int x0 = (int)pos.x + halfwidth/2;
		int x1 = (int)pos.x + halfwidth/2+halfwidth;
		
		if (x0 >= Game.WORLDW) x0 -= Game.WORLDW; 
		if (x0 < 0) x0 += Game.WORLDW;
		
		if (x1 >= Game.WORLDW) x1 -= Game.WORLDW;
		if (x1 < 0) x1 += Game.WORLDW;
		
		float h0 = ter.GetHeight(x0);
		float h1 = ter.GetHeight(x1);
		
		theta = -(float)Math.atan( (h1-h0)/(float)halfwidth );
		return (float)Math.toDegrees(theta);
	}
}
