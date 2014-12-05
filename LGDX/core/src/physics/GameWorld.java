package physics;

import java.util.Iterator;
import java.util.Vector;

import particles.Particles;
import particles.Weather;
import terrain.Background;
import terrain.FogOfWar;
import terrain.Terrain;
import ui.MenuBar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;
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
	private int armyid = 0;
	
	private CombatResolver resolver;
	
	private Terrain ter;
	private Particles particles;
	
	private Army userArmy;
	private Vector<Army> friendlyArmy;
	private Vector<Army> enemyArmy;
	private Vector<NullTank> nullTanks;
	
	public GameWorld(Terrain Ter)
	{
		ter = Ter;
		
		currentstage = MOVESELECT;
		particles = new Particles();
		resolver = new CombatResolver(this, ter, particles);
		
		userArmy = null;
		friendlyArmy = new Vector<Army>();
		enemyArmy = new Vector<Army>();
		nullTanks = new Vector<NullTank>();
	}
	
	public void Release()
	{
		ter.Release();
	}
	
	public Terrain GetTerrain()
	{
		return ter;
	}
	
	public Army GetArmy(int ID)
	{
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext()) {
			Army a = f.next();
			if (a.GetID() == ID) 
				return a;
		}
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext()) {
			Army a = e.next();
			if (a.GetID() == ID) 
				return a;
		}
		
		if (userArmy.GetID() == ID) {
			return userArmy;
		}
		
		return null;
	}
	
	public Army GetRemoteArmy(int Connection)
	{
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext()) {
			Army a = f.next();
			if (a.GetConnection() == Connection) 
				return a;
		}
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext()) {
			Army a = e.next();
			if (a.GetConnection() == Connection) 
				return a;
		}
		
		if (userArmy.GetConnection() == Connection) {
			return userArmy;
		}
		
		return null;
	}
	
	public void SetUserArmy(Army Add)
	{
		userArmy = Add;
		userArmy.SetID(armyid);
		armyid++;
	}
	
	public void AddFriendlyArmy(Army Add)
	{
		friendlyArmy.add(Add);
		friendlyArmy.lastElement().SetID(armyid);
		armyid++;
	}
	
	public void AddEnemyArmy(Army Add)
	{
		enemyArmy.add(Add);
		enemyArmy.lastElement().SetID(armyid);
		armyid++;
	}
	
	public void UpdateThreads()
	{
		// synchronize data between threads
		userArmy.UpdateThreads();
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().UpdateThreads();
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().UpdateThreads();
	}
	
	public void Update(Camera Cam)
	{
		UpdateThreads();
		ter.Update();
		particles.Update();
		
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
		
		UpdateNullTanks(Cam);
		CheckForDeaths(Cam);
		CheckNextStage();
	}
	
	public void UpdateNullTanks(Camera Cam)
	{
		Iterator<NullTank> t = nullTanks.iterator();
		while (t.hasNext())
			t.next().Update(Cam);
	}
	
	public void CheckForDeaths(Camera Cam)
	{
		userArmy.CheckForDeaths(Cam, nullTanks, particles);
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().CheckForDeaths(Cam, nullTanks, particles);
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().CheckForDeaths(Cam, nullTanks, particles);
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
		while (f.hasNext()) 
			f.next().UpdateAttackSelect(Cam);
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().UpdateAttackSelect(Cam);
	}
	
	private void UpdateAttack(Camera Cam)
	{
		// update the combat resolver
		resolver.UpdateSimulation();
	}
	
	public void ProcBlast(Blast B)
	{
		// process any blasts on all armies
		userArmy.ProcBlasts(B);
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().ProcBlasts(B);
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().ProcBlasts(B);
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
	
	private void DisableStencil(SpriteBatch Batch)
	{
		Batch.flush();
		Gdx.gl.glDisable(GL20.GL_STENCIL_TEST);
	}
	
	private void EnableStencil(SpriteBatch Batch)
	{
		Batch.flush();
		Gdx.gl.glEnable(GL20.GL_STENCIL_TEST);
	}
	
	private void DrawNullTanks(SpriteBatch Batch, Camera Cam)
	{
		Iterator<NullTank> t = nullTanks.iterator();
		while (t.hasNext()) 
			t.next().Draw(Batch, Cam);
	}
	
	private void DrawHidden(SpriteBatch Batch, Camera Cam)
	{
		// enable fog of war and draw the background
		FogOfWar.MaskOn(Batch);
		Background.DrawFG(Batch);
		Batch.flush();
		
		DisableStencil(Batch);
		
		// draw the weather and the bases
		userArmy.DrawBase(Batch, Cam);
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().DrawBase(Batch, Cam);
		
		EnableStencil(Batch);
		
		// draw the base logo's with the stencil test enabled
		userArmy.DrawBaseLogo(Batch, Cam);
		e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().DrawBaseLogo(Batch, Cam);
		
		DisableStencil(Batch);
		
		Weather.Draw(Batch, Cam);
		ter.Draw(Batch, Cam.GetPos());
		DrawNullTanks(Batch, Cam);
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().DrawBase(Batch, Cam);
		
		EnableStencil(Batch);
		
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
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().Draw(Batch, Cam, false, currentstage);
		
		userArmy.Draw(Batch, Cam, false, currentstage);
		DrawTargets(Batch, Cam);
		
		if (currentstage == ATTACKUPDATE) {
			resolver.DrawSimulation(Batch, Cam);
		}
		
		particles.Draw(Batch, Cam);
		MenuBar.Draw(Batch, Cam, currentstage, 
				(currentstage == MOVESELECT || currentstage == ATTACKSELECT) &&  !userArmy.IsMenuOpen() && !userArmy.IsStageCompleted(currentstage));
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
	
	public static int GetDirection(float StartX, float StartWidth, float TargetX, float TargetWidth)
	{
		// check the distance to the target in each direction
		float rdist = (Game.WORLDW-(StartX+StartWidth))+TargetX;
		if (TargetX > StartX+StartWidth)
			rdist = TargetX-(StartX+StartWidth);
		
		float ldist = StartX + (Game.WORLDW-(TargetX+TargetWidth));
		if (TargetX < StartX)
			ldist = (StartX-(TargetX+TargetWidth));
		
		if (rdist < ldist)
			return 1;
		else if (ldist < rdist)
			return -1;
		else
			return 0;
	}
	
	public static float GetDistance(float StartX, float StartWidth, float TargetX, float TargetWidth)
	{
		// check the distance to the target in each direction
		float rdist = (Game.WORLDW-(StartX+StartWidth))+TargetX;
		if (TargetX > StartX+StartWidth)
			rdist = TargetX-(StartX+StartWidth);
		
		float ldist = StartX + (Game.WORLDW-(TargetX+TargetWidth));
		if (TargetX < StartX)
			ldist = (StartX-(TargetX+TargetWidth));
		
		if (rdist < ldist)
			return rdist;
		else 
			return ldist;
	}
}