package terrain;

import particles.ParticleMask;
import particles.Particles;
import particles.Weather;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;
import com.mygdx.game.MilitaryBase;
import com.mygdx.game.Util;

public class Background 
{
	public static final Color DAY = new Color(74/255f, 69/255f, 61/255f, 1f);
	public static final Color NIGHT = new Color(64/255f, 61/255f, 74/255f, 1f);
	public static final Color NIGHTF = new Color(DAY.r, DAY.g, DAY.b, 0.15f);
	public static final Color TERRAIN = new Color(54/255f, 47/255f, 43/255f, 1f);
	public static final Color BUILDING = new Color(112/255f, 107/255f, 98/255f, 1f);
	
	private static Sound thunder;
	
	private static Texture bg;
	private static Texture fg;
	
	private static BGLayer l0;
	private static BGLayer l1;
	private static BGLayer l2;
	private static BGLayer l3;
	
	private static BGLayer c0;
	private static BGLayer c1;
	private static BGLayer c2;
	private static BGLayer c3;
	
	private static BGLayer nightg;
	private static BGLayer godrays;
	
	private static Texture light0;
	private static Texture light1;
	private static double lightclock = 0f;
	private static double nextlight;
	private static int lightlive = -1;
	
	private static Particles p;
	private static double clock;
	
	public static void release()
	{
		if (bg != null)
			bg.dispose();
		
		if (fg != null)
			fg.dispose();
		
		if (light0 != null)
			light0.dispose();
		
		if (light1 != null)
			light1.dispose();
		
		l0.release();
		l1.release();
		l2.release();
		l3.release();
		
		c0.release();
		c1.release();
		c2.release();
		c3.release();
		
		nightg.release();
		godrays.release();
	}
	
	public static void init()
	{
		if (bg == null) {
			Pixmap tmp = new Pixmap(Game.SCREENW, Game.SCREENH, Pixmap.Format.RGB888);
			tmp.setColor( Color.WHITE );
			tmp.fill();
			bg = new Texture(tmp);
			tmp.dispose();
		}
		
		if (fg == null) {
			Pixmap tmp = new Pixmap(Game.SCREENW, Game.SCREENH, Pixmap.Format.RGB888);
			tmp.setColor( new Color(0f, 0f, 0f, 1f));
			tmp.fill();
			fg = new Texture(tmp);
			tmp.dispose();
		}
		
		p = new Particles();
		
		c0 = new BGLayer("img/bg/clouds0.png", Util.brightenColor(DAY, 0.08f), 0.10f, 16f);
		c1 = new BGLayer("img/bg/clouds1.png", Util.brightenColor(DAY, 0.06f), 0.08f, 8f);
		c2 = new BGLayer("img/bg/clouds2.png", Util.brightenColor(DAY, 0.04f), 0.06f, 4f);
		c3 = new BGLayer("img/bg/clouds3.png", Util.brightenColor(DAY, 0.02f), 0.04f, 2f);
		
		l0 = new BGLayer("img/bg/layer0.png", Util.brightenColor(TERRAIN, 0.12f), 0.5f, 0f);
		l1 = new BGLayer("img/bg/layer1.png", Util.brightenColor(TERRAIN, 0.14f), 0.35f, 0f);
		l2 = new BGLayer("img/bg/layer2.png", Util.brightenColor(TERRAIN, 0.16f), 0.2f, 0f);
		l3 = new BGLayer("img/bg/layer3.png", Util.brightenColor(TERRAIN, 0.19f), 0.12f, 0f);
		
		l0.setNightColor( Util.darkenColor( Util.mergeColors(l0.getColor(), 0.2f, NIGHT), 0.05f));
		l1.setNightColor( Util.darkenColor( Util.mergeColors(l1.getColor(), 0.2f, NIGHT), 0.05f));
		l2.setNightColor( Util.darkenColor( Util.mergeColors(l2.getColor(), 0.2f, NIGHT), 0.05f));
		l3.setNightColor( Util.darkenColor( Util.mergeColors(l3.getColor(), 0.2f, NIGHT), 0.05f));
		
		nightg = new BGLayer("img/bg/night_gradient.png", NIGHTF, 0f, 0f);
		godrays = new BGLayer("img/bg/god_rays.png", new Color(1f, 1f, 1f, 1.0f), 0.03f, 0f);
		
		light0 = new Texture( Gdx.files.internal("img/bg/lightning0.png") );
		light1 = new Texture( Gdx.files.internal("img/bg/lightning1.png") );
		
		nextlight = Math.random()*4f;
		
		thunder = Gdx.audio.newSound(Gdx.files.internal("aud/sfx/thunder.wav"));
		
		Terrain.setColor( TERRAIN );
	}
	
	public static void update(Camera Cam)
	{
		lightclock += Gdx.graphics.getDeltaTime();
		if (lightlive == -1) {
			if (lightclock > nextlight) {
				Cam.addShakeIntensity( (float)(Math.random()*1f) + 1f );
				
				if (Game.SOUND)
					thunder.play( (float)(Math.random()*0.2f + 0.1f) );
				lightlive = (int)(Math.random()*2);
				lightclock = 0f;
			}
		} else if (lightclock > 0.5f) {
			nextlight = Math.random()*6f;
			lightclock = 0f;
			lightlive = -1;
		}
		
		
		l0.update(Cam);
		l1.update(Cam);
		l2.update(Cam);
		l3.update(Cam);
		
		c0.update(Cam);
		c1.update(Cam);
		c2.update(Cam);
		c3.update(Cam);
		
		godrays.update(Cam);
		
		clock += Gdx.graphics.getDeltaTime();
		if (clock > 0.1f) {
			clock = 0f;
			
			for (int i=0; i<4; i++) {
				p.addParticle((float)Math.random()*16+16, new Vector2((float)Math.random()*100, Game.WORLDH-650));
				p.addParticle((float)Math.random()*16+16, new Vector2(100 + (float)Math.random()*100, Game.WORLDH-650));
				p.addParticle((float)Math.random()*16+16, new Vector2(200 + (float)Math.random()*100, Game.WORLDH-650));
				p.addParticle((float)Math.random()*16+16, new Vector2(300 + (float)Math.random()*100, Game.WORLDH-650));
				p.addParticle((float)Math.random()*16+16, new Vector2(400 + (float)Math.random()*100, Game.WORLDH-650));
				p.addParticle((float)Math.random()*16+16, new Vector2(500 + (float)Math.random()*100, Game.WORLDH-650));
				p.addParticle((float)Math.random()*16+16, new Vector2(600 + (float)Math.random()*100, Game.WORLDH-650));
				p.addParticle((float)Math.random()*16+16, new Vector2(700 + (float)Math.random()*100, Game.WORLDH-650));
				p.addParticle((float)Math.random()*16+16, new Vector2(800 + (float)Math.random()*100, Game.WORLDH-650));
			}
		}
		
		p.update();
	}
	
	private static void setCloudAlpha(float A)
	{
		c0.setAlpha(A);
		c1.setAlpha(A);
		c2.setAlpha(A);
		c3.setAlpha(A);
	}
	
	private static void setGlobalColors()
	{
		if (TimeOfDay.isNight()) {
			Color night = Util.mergeColors(TERRAIN, 0.2f, NIGHT);
			Terrain.setColor(night);
			
		} else if (TimeOfDay.isDay()) {
			Terrain.setColor( TERRAIN );
			
		} else {
			Color night = Util.mergeColors(TERRAIN, 0.2f, NIGHT);
			float d = TimeOfDay.getTrans();
			float n = 1f - d;
			
			Terrain.setColor( new Color(TERRAIN.r*d + night.r*n, TERRAIN.g*d + night.g*n, TERRAIN.b*d + night.b*n, 1f) );
		}
		
		MilitaryBase.BGCOLOR = Terrain.getColor();
	}
	
	public static void drawBG(SpriteBatch Batch, Camera Cam)
	{
		setGlobalColors();
		
		if (TimeOfDay.isNight())
			Batch.setColor( NIGHT );
		else if (TimeOfDay.isDay())
			Batch.setColor( DAY );
		else {
			float d = TimeOfDay.getTrans();
			float n = 1f - d;
			
			Batch.setColor( new Color(DAY.r*d + NIGHT.r*n, DAY.g*d + NIGHT.g*n, DAY.b*d + NIGHT.b*n, 1f));
		}
		
		Batch.draw(bg, 0, 0, Game.SCREENW, Game.SCREENH);
		Batch.setColor( Color.WHITE );
		
		if (TimeOfDay.isNight()) {
			Sky.setAlpha(1f);
			nightg.setAlpha(1f);
			nightg.draw(Batch, Cam);
			Sky.draw(Batch);
		} else if (TimeOfDay.isTrans()) {
			nightg.setAlpha( -(TimeOfDay.getTrans()) + 1f );
			nightg.draw(Batch, Cam);
			
			Sky.setAlpha( -(TimeOfDay.getTrans()) + 1f );
			Sky.draw(Batch);
		}
		
		if (!TimeOfDay.isNight()) {
			if (TimeOfDay.isTrans())
				setCloudAlpha( 0.2f * (TimeOfDay.getTrans()*0.95f + 0.05f) );
			else
				setCloudAlpha( 0.2f );
			
		} else {
			setCloudAlpha(0.05f);
		}
		
		c3.draw(Batch, Cam);
		c2.draw(Batch, Cam);
		c1.draw(Batch, Cam);
		c0.draw(Batch, Cam);
		
		if (!TimeOfDay.isNight()) {
			if (TimeOfDay.isTrans())
				godrays.setAlpha( 0.3f * TimeOfDay.getTrans());
			else
				godrays.setAlpha(0.3f);
			
			godrays.draw(Batch, Cam);
		}
		
		Texture active = null;
		if (lightlive == 0)
			active = light0;
		else if (lightlive == 1)
			active = light1;
		
		if (active != null) {
			Batch.draw(active, l3.getPos() - active.getWidth()*2f, Cam.getRenderY(Game.WORLDH - active.getHeight()));
			Batch.draw(active, l3.getPos() - active.getWidth(), Cam.getRenderY(Game.WORLDH - active.getHeight()));
			Batch.draw(active, l3.getPos(), Cam.getRenderY(Game.WORLDH - active.getHeight()));
			Batch.draw(active, l3.getPos() + active.getWidth(), Cam.getRenderY(Game.WORLDH - active.getHeight()));
			Batch.draw(active, l3.getPos() + active.getWidth()*2f, Cam.getRenderY(Game.WORLDH - active.getHeight()));
		}
		
		l3.draw(Batch, Cam);
		l2.draw(Batch, Cam);
		
		ParticleMask.setOpacity(0.2f);
		p.drawParallax(Batch, Cam, l1.getPos() - l1.getWidth()*2f);
		p.drawParallax(Batch, Cam, l1.getPos() - l1.getWidth());
		p.drawParallax(Batch, Cam, l1.getPos());
		p.drawParallax(Batch, Cam, l1.getPos() + l1.getWidth());
		p.drawParallax(Batch, Cam, l1.getPos() + l1.getWidth()*2f);
		ParticleMask.setOpacity(1f);
		
		l1.draw(Batch, Cam);
	  	l0.draw(Batch, Cam);
	  	
	  	Weather.draw(Batch, Cam);
	}
	
	public static void drawFG(SpriteBatch Batch, Camera Cam)
	{
		Batch.setColor(0f, 0f, 0f, 0.15f);
		Batch.draw(fg, 0, 0, Game.SCREENW, Game.SCREENH);
		Batch.setColor(Color.WHITE);
	}
}
