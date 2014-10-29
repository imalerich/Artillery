package physics;

import java.util.Iterator;
import java.util.Vector;

import terrain.Background;
import terrain.FogOfWar;
import terrain.Terrain;
import ui.MenuBar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Camera;
import com.mygdx.game.Shaders;

import entity.Army;
import entity.SelectionStack;

public class GameWorld 
{
	public static final int STAGECOUNT = 4;
	public static final int MOVESELECT = 0;
	public static final int MOVEUPDATE = 1;
	public static final int ATTACKSELECT = 2;
	public static final int ATTACKUPDATE = 3;
	private int currentstage;
	
	private CombatResolver resolver;
	
	private Terrain ter;
	private Army userArmy;
	private Vector<Army> friendlyArmy;
	private Vector<Army> enemyArmy;
	
	public GameWorld(Terrain Ter)
	{
		ter = Ter;
		currentstage = MOVESELECT;
		
		resolver = new CombatResolver();
		userArmy = null;
		friendlyArmy = new Vector<Army>();
		enemyArmy = new Vector<Army>();
	}
	
	public void Release()
	{
		ter.Release();
	}
	
	public Terrain GetTerrain()
	{
		return ter;
	}
	
	public void SetUserArmy(Army Add)
	{
		userArmy = Add;
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
		userArmy.UpdateMoveSelect(Cam);
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().UpdateMoveSelect(Cam);
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().UpdateMoveSelect(Cam);
	}
	
	public void UpdateMove(Camera Cam)
	{
		userArmy.UpdateMove(Cam);
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().UpdateMove(Cam);
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().UpdateMove(Cam);
	}
	
	public void UpdateAttackSelect(Camera Cam)
	{
		userArmy.UpdateAttackSelect(Cam);
		if (userArmy.IsTargeting()) {
			BuildTargetStack(Cam);
		}
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext()) {
			Army army = f.next();
			army.UpdateAttackSelect(Cam);
		}
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().UpdateAttackSelect(Cam);
	}
	
	private void UpdateAttack(Camera Cam)
	{
		resolver.UpdateSimulation();
	}
	
	private int GetTargetSize(Camera Cam)
	{
		int count = 0;
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			count += e.next().GetMouseOverCount(Cam);
		
		return count;
	}
	
	private void BuildTargetStack(Camera Cam)
	{
		// do not rebuilt the target stack when the stack size does not change
		if (!userArmy.UpdateTargetOptions( GetTargetSize(Cam) ))
			return;
		
		// get all squad options from the mouse over
		SelectionStack stack = userArmy.GetTargetOptions();
		stack.Reset();
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext()) {
			e.next().GetMouseOver(stack, Cam);
		}
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
			UpdateAttackSelect(Cam);
			break;
			
		case ATTACKUPDATE:
			UpdateAttack(Cam);
			break;
			
		default:
			break;
		}
		
		CheckNextStage();
	}
	
	private void DrawTargets(SpriteBatch Batch, Camera Cam)
	{
		if (currentstage != MOVESELECT && currentstage != ATTACKSELECT)
			return;
		
		if (currentstage == MOVESELECT) {
			userArmy.DrawTargetPos(Batch, Cam);
		} else if (currentstage == ATTACKSELECT) {
			userArmy.DrawTargetSquad(Batch, Cam);
		}
	}
	
	private void DrawFogMask(SpriteBatch Batch, Camera Cam)
	{
		FogOfWar.Begin(Batch);
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().DrawView(Cam);
		userArmy.DrawView(Cam);
		
		FogOfWar.End(Batch);
	}
	
	private boolean CheckTargets()
	{
		if (currentstage != ATTACKSELECT)
			return false;
		
		if (userArmy.IsTargeting())
			return true;
		
		return false;
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
		userArmy.DrawBase(Batch, Cam);
		Batch.flush();
		Gdx.gl.glEnable(GL20.GL_STENCIL_TEST);
		
		// draw all enemy untis above the terrain, but hidden by the fog 
		Shaders.SetShader(Batch, Shaders.enemy);
		e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().Draw(Batch, Cam, CheckTargets(), currentstage);
		Shaders.RevertShader(Batch);
		
		FogOfWar.MaskOff(Batch);
	}
	
	public void Draw(SpriteBatch Batch, Camera Cam)
	{
		Background.DrawBG(Batch);
		DrawFogMask(Batch, Cam);
		DrawHidden(Batch, Cam);
		
		DrawTargets(Batch, Cam);
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().Draw(Batch, Cam, false, currentstage);
		
		userArmy.Draw(Batch, Cam, false, currentstage);
		
		if (currentstage == ATTACKUPDATE) {
			resolver.DrawSimulation(Batch, Cam);
		}
		
		MenuBar.Draw(Batch, Cam, currentstage, (currentstage == MOVESELECT || currentstage == ATTACKSELECT));
	}
	
	private boolean IsArmiesStageCompleted()
	{
		Iterator<Army> f = friendlyArmy.iterator();
		Iterator<Army> e = enemyArmy.iterator();
		
		if (!userArmy.IsStageCompleted(currentstage))
			return false;
		
		// loop through each army and check if any of them are not completed
		while (f.hasNext()) {
			if (!f.next().IsStageCompleted(currentstage))
				return false;
		}

		while (e.hasNext()) {
			if (!e.next().IsStageCompleted(currentstage))
				return false;
		}
		
		if (currentstage == ATTACKUPDATE && !resolver.IsSimulationCompleted()) {
			return false;
		}
		
		return true;
	}
	
	private void InitResolver()
	{
		if (currentstage != ATTACKUPDATE) {
			return;
		}
		
		resolver.StartSimulation();
		Iterator<Army> f = friendlyArmy.iterator();
		Iterator<Army> e = enemyArmy.iterator();
		
		userArmy.AddCombatData(resolver);
		while (f.hasNext()) {
			f.next().AddCombatData(resolver);
		}
		
		while (e.hasNext()) {
			e.next().AddCombatData(resolver);
		}
	}
	
	private void InitNewStage()
	{
		Iterator<Army> f = friendlyArmy.iterator();
		Iterator<Army> e = enemyArmy.iterator();

		userArmy.InitStage(currentstage);
		
		while (f.hasNext())
			f.next().InitStage(currentstage);

		while (e.hasNext())
			e.next().InitStage(currentstage);
		
		InitResolver();
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