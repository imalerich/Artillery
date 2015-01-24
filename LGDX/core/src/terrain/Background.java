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

public class Background {
	public static final Color FGCOLOR = new Color(99/255f, 33/255f, 28/255f, 1f);
	//public static final Color FGCOLOR = new Color(66/255f, 108/255f, 160/255f, 1f);
	public static final Color GRASS = Terrain.getColor();
	
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
		
		godrays.release();
	}
	
	public static void init()
	{
		if (bg == null) {
			Pixmap tmp = new Pixmap(Game.SCREENW, Game.SCREENH, Pixmap.Format.RGB888);
			tmp.setColor( FGCOLOR );
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
		
		l0 = new BGLayer("img/bg/layer0.png", new Color(GRASS.r * 1.4f, GRASS.g * 1.4f, GRASS.b * 1.4f, 1f), 0.5f, 0f);
		l1 = new BGLayer("img/bg/layer1.png", new Color(GRASS.r * 1.6f, GRASS.g * 1.6f, GRASS.b * 1.6f, 1f), 0.4f, 0f);
		l2 = new BGLayer("img/bg/layer2.png", new Color(GRASS.r * 1.8f, GRASS.g * 1.8f, GRASS.b * 1.8f, 1f), 0.2f, 0f);
		l3 = new BGLayer("img/bg/layer3.png", new Color(GRASS.r * 0.8f, GRASS.g * 0.8f, GRASS.b * 0.8f, 1f), 0.12f, 0f);
		
		float scale = 0.2f;
		c0 = new BGLayer("img/bg/clouds0.png", new Color(FGCOLOR.r + (scale), FGCOLOR.g + (scale), FGCOLOR.b + (scale), 1f), 0.10f, 16f);
		c1 = new BGLayer("img/bg/clouds1.png", new Color(FGCOLOR.r + (scale*0.8f), FGCOLOR.g + (scale*0.8f), FGCOLOR.b + (scale*0.8f), 1f), 0.08f, 8f);
		c2 = new BGLayer("img/bg/clouds2.png", new Color(FGCOLOR.r + (scale*0.5f), FGCOLOR.g + (scale*0.5f), FGCOLOR.b + (scale*0.5f), 1f), 0.06f, 4f);
		c3 = new BGLayer("img/bg/clouds3.png", new Color(FGCOLOR.r + (scale*0.3f), FGCOLOR.g + (scale*0.3f), FGCOLOR.b + (scale*0.3f), 1f), 0.04f, 2f);
		
		godrays = new BGLayer("img/bg/god_rays.png", new Color(1f, 1f, 1f, 0.4f), 0.07f, 0f);
		
		light0 = new Texture( Gdx.files.internal("img/bg/lightning0.png") );
		light1 = new Texture( Gdx.files.internal("img/bg/lightning1.png") );
		
		nextlight = Math.random()*4f;
		
		thunder = Gdx.audio.newSound(Gdx.files.internal("aud/sfx/thunder.wav"));
	}
	
	public static void update(Camera Cam)
	{
		lightclock += Gdx.graphics.getDeltaTime();
		if (lightlive == -1) {
			if (lightclock > nextlight) {
				Cam.addShakeIntensity( (float)(Math.random()*1f) + 1f );
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
	
	public static void drawBG(SpriteBatch Batch, Camera Cam)
	{
		Batch.draw(bg, 0, 0, Game.SCREENW, Game.SCREENH);
		
		c3.draw(Batch, Cam);
		c2.draw(Batch, Cam);
		c1.draw(Batch, Cam);
		c0.draw(Batch, Cam);
	  	godrays.draw(Batch, Cam);
		
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
		
		ParticleMask.setOpacity(0.3f);
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
