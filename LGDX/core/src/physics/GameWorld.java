package physics;

import java.util.Iterator;
import java.util.Vector;

import terrain.Background;
import terrain.FogOfWar;
import terrain.Terrain;
import ui.MenuBar;
import arsenal.Projectile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Camera;
import com.mygdx.game.Shaders;

import entity.Army;

public class GameWorld 
{
	public static final int STAGECOUNT = 4;
	public static final int MOVESELECT = 0;
	public static final int MOVEUPDATE = 1;
	public static final int ATTACKSELECT = 2;
	public static final int ATTACKUPDATE = 3;
	private int currentstage;
	
	private Terrain ter;
	private Vector<Army> friendlyArmy;
	private Vector<Army> enemyArmy;
	private Vector<Projectile> projectiles;
	
	public GameWorld(Terrain Ter)
	{
		ter = Ter;
		currentstage = MOVESELECT;
		
		friendlyArmy = new Vector<Army>();
		enemyArmy = new Vector<Army>();
		projectiles = new Vector<Projectile>();
	}
	
	public void Release()
	{
		ter.Release();
	}
	
	public Terrain GetTerrain()
	{
		return ter;
	}
	
	public void AddProjectile(Projectile Add)
	{
		projectiles.add(Add);
	}
	
	public void AddFriendlyArmy(Army Add)
	{
		friendlyArmy.add(Add);
	}
	
	public void AddEnemyArmy(Army Add)
	{
		enemyArmy.add(Add);
	}
	
	public void UpdateMoveSelect(Camera Cam)
	{
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().UpdateMoveSelect(Cam);
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().UpdateMoveSelect(Cam);
	}
	
	public void UpdateMove(Camera Cam)
	{
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().UpdateMove(Cam);
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().UpdateMove(Cam);
		
		Iterator<Projectile> p = projectiles.iterator();
		while (p.hasNext())
			p.next().Update();
	}
	
	public void Update(Camera Cam)
	{
		ter.Update();
		
		switch (currentstage)
		{
		case MOVESELECT:
			UpdateMoveSelect(Cam);
			break;
			
		case MOVEUPDATE:
			UpdateMove(Cam);
			break;
			
		case ATTACKSELECT:
			break;
			
		case ATTACKUPDATE:
			break;
			
		default:
			break;
		}
		
		CheckNextStage();
	}
	
	private void DrawTargets(SpriteBatch Batch, Camera Cam)
	{
		if (currentstage != MOVESELECT)
			return;
		
		// only friendly units can have target positions drawn
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().DrawTargets(Batch, Cam);
	}
	
	private void DrawFogMask(SpriteBatch Batch, Camera Cam)
	{
		FogOfWar.Begin(Batch);
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().DrawView(Cam);
		
		FogOfWar.End(Batch);
	}
	
	private void DrawHidden(SpriteBatch Batch, Camera Cam)
	{
		// enable fog of war and draw the background
		FogOfWar.MaskOn(Batch);
		Background.DrawFG(Batch);
		Batch.flush();
		
		// temporarily disable the stencil test to draw the terrain
		Gdx.gl.glDisable(GL20.GL_STENCIL_TEST);
		ter.Draw(Batch, Cam.GetPos());
		Batch.flush();
		Gdx.gl.glEnable(GL20.GL_STENCIL_TEST);
		
		// draw enemy base hidden by fog
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().DrawBase(Batch, Cam);
		
		// temporarily disable the stencil test to draw the friendly terrain
		Batch.flush();
		Gdx.gl.glDisable(GL20.GL_STENCIL_TEST);
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().DrawBase(Batch, Cam);
		Batch.flush();
		Gdx.gl.glEnable(GL20.GL_STENCIL_TEST);
		
		// draw all enemy untis above the terrain, but hidden by the fog 
		Shaders.SetShader(Batch, Shaders.enemy);
		e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().Draw(Batch, Cam);
		Shaders.RevertShader(Batch);
		
		FogOfWar.MaskOff(Batch);
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam)
	{
		Background.DrawBG(Batch);
		DrawFogMask(Batch, Cam);
		DrawHidden(Batch, Cam);
		
		DrawTargets(Batch, Cam);
		
		Iterator<Projectile> p = projectiles.iterator();
		while (p.hasNext())
			p.next().Draw(Batch, Cam);
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().Draw(Batch, Cam);
		
		MenuBar.Draw(Batch, Cam);
	}
	
	private boolean IsArmiesStageCompleted()
	{
		Iterator<Army> f = friendlyArmy.iterator();
		Iterator<Army> e = enemyArmy.iterator();
		
		// loop through each army and check if any of them are not completed
		while (f.hasNext()) {
			if (!f.next().IsStageCompleted(currentstage))
				return false;
		}

		while (e.hasNext()) {
			if (!e.next().IsStageCompleted(currentstage))
				return false;
		}
		
		return true;
	}
	
	private void InitNewStage()
	{
		Iterator<Army> f = friendlyArmy.iterator();
		Iterator<Army> e = enemyArmy.iterator();

		while (f.hasNext())
			f.next().InitStage();

		while (e.hasNext())
			e.next().InitStage();
	}
	
	public void CheckNextStage()
	{
		// if all the armies are ready to update update the current stage
		if ( !IsArmiesStageCompleted() )
			return;
		
		// set the new stage
		currentstage++;
		if (currentstage == STAGECOUNT)
			currentstage = 0;
		
		InitNewStage();
	}
}