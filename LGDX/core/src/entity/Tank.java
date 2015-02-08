package entity;

import java.util.Vector;

import particles.Particles;
import physics.NullTank;
import terrain.Terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Cursor;
import com.mygdx.game.Game;
import com.mygdx.game.Shaders;

import config.SquadConfigurations;

public class Tank extends Unit 
{
	private static float MAXANGLE = 180+15;
	private static float MINANGLE = -15;
	public static Texture BARREL;
	
	private Texture tex;
	private TextureRegion tr;
	
	private Vector2 barrelOffset; // coordinates of the barrel relative to the tank
	
	protected float barrelPhi = 0.0f;
	private int halfwidth;
	private int barrelwidth;
	private int barrelheight;
	
	public static void init()
	{
		if (BARREL == null) {
			BARREL = new Texture( Gdx.files.internal("img/tanks/Barrel.png") );
		}
	}
	
	public static int getBarrelWidth()
	{
		if (BARREL != null) {
			return BARREL.getWidth();
		} else {
			return 0;
		}
	}
	
	@Override
	public void release()
	{
		tex.dispose();
		
		if (BARREL != null)
			BARREL.dispose();
	}
	
	public Tank(String Filename, Terrain Ter, int Speed, int Health)
	{
		tex = new Texture(Gdx.files.internal(Filename) );
		tr = new TextureRegion(tex);
		
		halfwidth = tex.getWidth()/2;
		width = tex.getWidth();
		height = tex.getHeight();
		
		barrelwidth = BARREL.getWidth();
		barrelheight = BARREL.getHeight();
		
		pos = new Vector2(64, 0);
		pos.y = Game.WORLDH - Ter.getHeight((int)pos.x+halfwidth) - 3;
		
		barrelOffset = new Vector2();		

		forward = true;
		barrelPhi = 0.0f;
		ter = Ter;
		speed = Speed;
		health = Health;
		maxhealth = Health;
		
		setReqBonus( SquadConfigurations.getConfiguration(SquadConfigurations.TANK).reqbonus );
	}
	
	@Override
	public Rectangle getBBox()
	{
		// get the tanks angle
		float theta = (float)Math.toRadians( getAngle() );
		
		// get the points describing the boundaries
		Vector2[] coords = new Vector2[4];
		coords[0] = new Vector2(-width/2f, 0f);
		coords[1] = new Vector2(-width/2f, height);
		
		coords[2]= new Vector2(width/2f, 0f);
		coords[3]= new Vector2(width/2f, height);
		
		// rotate the coordinates we are using to describe the bounding box
		for (int i=0; i<4; i++) {
			coords[i] = rotateCoord(coords[i], theta);
		}
		
		// get the min and maxes from these coordinates
		Vector2 min = new Vector2(Float.MAX_VALUE, Float.MAX_VALUE);
		Vector2 max = new Vector2(Float.MIN_VALUE, Float.MIN_VALUE);
		
		for (int i=0; i<4; i++) {
			if (coords[i].x < min.x)
				min.x = coords[i].x;
			
			if (coords[i].x > max.x)
				max.x = coords[i].x;
			
			if (coords[i].y < min.y)
				min.y = coords[i].y;
			
			if (coords[i].y > max.y)
				max.y = coords[i].y;
		}
		
		// return an axis aligned bounding box encompasing the tank
		Rectangle r = new Rectangle(min.x + pos.x + width/2f, min.y + pos.y, max.x-min.x, max.y-min.y);
		if (r.x < 0)
			r.x += Game.WORLDW;
		else if (r.x > Game.WORLDW)
			r.x -= Game.WORLDW;
		
		return r;
	}
	
	public void setAsDeceased(Vector<NullTank> Deceased, Particles Part)
	{
		Deceased.add(createNullTank(Part));
	}
	
	public NullTank createNullTank(Particles Part)
	{
		return new NullTank(Part, ter, tex, barrelOffset, pos, barrelPhi, forward);
	}
	
	public void setBarrelOffset(Vector2 Offset)
	{
		barrelOffset = Offset;
	}
	
	@Override
	public float getAngle()
	{
		float theta = 0.0f;
		int x0 = (int)pos.x + halfwidth/2;
		int x1 = (int)pos.x + halfwidth/2+halfwidth;
		
		if (x0 >= Game.WORLDW) x0 -= Game.WORLDW; 
		if (x0 < 0) x0 += Game.WORLDW;
		
		if (x1 >= Game.WORLDW) x1 -= Game.WORLDW;
		if (x1 < 0) x1 += Game.WORLDW;
		
		float h0 = ter.getHeight(x0);
		float h1 = ter.getHeight(x1);
		
		theta = -(float)Math.atan( (h1-h0)/(float)halfwidth );
		return (float)Math.toDegrees(theta);
	}
	
	@Override
	public float getBarrelAbsoluteAngle()
	{
		if (forward) {
			return barrelPhi + getAngle();
		} else {
			return barrelPhi - getAngle();
		}
	}
	
	@Override
	public void setBarrelAngle(float Angle)
	{
		if (forward) {
			barrelPhi = Angle - getAngle();
		} else {
			barrelPhi = Angle + getAngle();
		}
		
		// clamp the angle
		barrelPhi = Math.max(barrelPhi, MINANGLE);
		barrelPhi = Math.min(barrelPhi, MAXANGLE);
	}
	
	private void drawOutline(SpriteBatch Batch, Camera Cam)
	{
		for (int x=-1; x<2; x++) {
			for (int y=-1; y<2; y++) {
				renderBarrel(Batch, Cam, x, y);
				render(Batch, Cam, x, y, height);
			}
		}
	}
	
	private void DrawTarget(SpriteBatch Batch, Camera Cam)
	{
		Shaders.setShader(Batch, Shaders.target);
		drawOutline(Batch, Cam);
		Shaders.revertShader(Batch);
	}
	
	private void DrawHighlight(SpriteBatch Batch, Camera Cam)
	{
		Shaders.setShader(Batch, Shaders.hili);
		drawOutline(Batch, Cam);
		Shaders.revertShader(Batch);
	}
	
	@Override
	public void draw(SpriteBatch Batch, Camera Cam, boolean Highlight, boolean Target)
	{
		setHeight();
		if (Highlight)
			DrawHighlight(Batch, Cam);
		else if (Target)
			DrawTarget(Batch, Cam);
		
		Batch.setColor(getSquad().getArmy().unitcolor);
		renderBarrel(Batch, Cam, 0, 0);
		render(Batch, Cam, 0, 0, height);
		Batch.setColor(Color.WHITE);
		
		boolean drawhealth = true;
		if (getSquad().isStealthed() && !(getSquad().getArmy() instanceof UserArmy)) {
			drawhealth = false;
		}
		
		// draw the tanks health
		if (Cursor.isMouseOver(getBBox(), Cam.getPos()) && drawhealth) {
			Shaders.setShader(Batch, Shaders.health);
			int h = (int)(height * (float)health/maxhealth);
			
			render(Batch, Cam, 0, 0, h);
			Shaders.revertShader(Batch);
		}
	}
	
	private void render(SpriteBatch Batch, Camera Cam, int OffsetX, int OffsetY, int SrcHeight)
	{
		float theta = getAngle();
		
		// draw the tank
		tr.setRegion(0, height-SrcHeight, width, SrcHeight);
		tr.flip(!forward, false);
		
		Batch.draw(tr, Cam.getRenderX(pos.x + OffsetX), 
				Cam.getRenderY(pos.y + OffsetY), halfwidth, 0, width, SrcHeight, 1f, 1f, theta);
	}
	
	private void renderBarrel(SpriteBatch Batch, Camera Cam, int OffsetX, int OffsetY)
	{
		float theta = getAngle();
		float phi = barrelPhi;
		if (!forward)
			phi = 180 - phi;
		
		// draw the tanks barrel
		Vector2 offset = new Vector2(barrelOffset.x-halfwidth, barrelOffset.y);
		if (!forward) offset.x = halfwidth - barrelOffset.x;
		offset = rotateCoord( offset, (float)Math.toRadians(theta) );
		
		// draw the tanks barrel
		Batch.draw(BARREL, Cam.getRenderX(pos.x + halfwidth + offset.x + OffsetX),
				Cam.getRenderY(pos.y + offset.y + OffsetY),
				0, barrelheight/2f, barrelwidth, barrelheight, 1f, 1f, 
				phi + theta, 0, 0, barrelwidth, barrelheight, false, false);
	}
	
	@Override
	public void drawTargetAngle(SpriteBatch Batch, Camera Cam)
	{
		float theta = getAngle();
		float phi = barrelPhi;
		
		float width = Squad.target.getFrameWidth();
		float height = Squad.target.getFrameHeight();
		Vector2 src = new Vector2(barrelOffset.x-halfwidth, 
				barrelOffset.y);
		if (!forward)
			src.x = halfwidth - barrelOffset.x;
		src = rotateCoord( src, (float)Math.toRadians(theta));
		
		Vector2 offset = new Vector2(barrelwidth*1.8f, 0f);
		if (!forward) {
			offset.x = -barrelwidth*1.8f;
			phi = -phi;
		}
		
		offset = rotateCoord( offset, (float)Math.toRadians(phi + theta) );
		offset.x += src.x;
		offset.y += src.y;
		
		animtime += Gdx.graphics.getDeltaTime();
		Squad.target.setTime(animtime);
		Batch.draw(Squad.target.getCurrent(0), Cam.getRenderX(pos.x + halfwidth + offset.x - width/2f),
				Cam.getRenderY(pos.y + offset.y - height/2f));
	}
}
