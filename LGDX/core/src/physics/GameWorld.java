package physics;

import java.util.Iterator;
import java.util.Vector;

import objects.FoxHole;
import objects.TankBarrier;
import particles.Particles;
import particles.Weather;
import terrain.Background;
import terrain.FogOfWar;
import terrain.Terrain;
import ui.MenuBar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Game;

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
	private Vector<FoxHole> foxholes;
	private Vector<TankBarrier> barriers;
	
	public GameWorld(Terrain Ter)
	{
		ter = Ter;
		
		currentstage = MOVESELECT;
		particles = new Particles();
		resolver = new CombatResolver(this, ter, particles);
		
		userArmy = null;
		friendlyArmy	= new Vector<Army>();
		enemyArmy		= new Vector<Army>();
		nullTanks		= new Vector<NullTank>();
		foxholes		= new Vector<FoxHole>();
		barriers		= new Vector<TankBarrier>();
	}
	
	public void release()
	{
		ter.release();
	}
	
	public Terrain getTerrain()
	{
		return ter;
	}
	
	public Army getArmy(int ID)
	{
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext()) {
			Army a = f.next();
			if (a.getID() == ID) 
				return a;
		}
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext()) {
			Army a = e.next();
			if (a.getID() == ID) 
				return a;
		}
		
		if (userArmy.getID() == ID) {
			return userArmy;
		}
		
		return null;
	}
	
	public Army getRemoteArmy(int Connection)
	{
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext()) {
			Army a = f.next();
			if (a.getConnection() == Connection) 
				return a;
		}
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext()) {
			Army a = e.next();
			if (a.getConnection() == Connection) 
				return a;
		}
		
		if (userArmy.getConnection() == Connection) {
			return userArmy;
		}
		
		return null;
	}
	
	public void setUserArmy(Army Add)
	{
		userArmy = Add;
		userArmy.setID(armyid);
		armyid++;
	}
	
	public void addFriendlyArmy(Army Add)
	{
		friendlyArmy.add(Add);
		friendlyArmy.lastElement().setID(armyid);
		armyid++;
	}
	
	public void addEnemyArmy(Army Add)
	{
		enemyArmy.add(Add);
		enemyArmy.lastElement().setID(armyid);
		armyid++;
	}
	
	public void addFoxHole(Vector2 Pos)
	{
		foxholes.add(new FoxHole(Pos));
	}
	
	public void addTankBarrier(Vector2 Pos)
	{
		barriers.add(new TankBarrier(Pos, ter));
	}
	
	public Iterator<FoxHole> getFoxHoles()
	{
		return foxholes.iterator();
	}
	
	public Iterator<TankBarrier> getBarriers()
	{
		return barriers.iterator();
	}
	
	public void updateThreads(Camera Cam)
	{
		// synchronize data between threads
		userArmy.updateThreads(Cam);
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().updateThreads(Cam);
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().updateThreads(Cam);
	}
	
	public void update(Camera Cam)
	{
		updateThreads(Cam);
		ter.update();
		particles.update();
		
		switch (currentstage)
		{
		case MOVESELECT:
			updateMoveSelect(Cam);
			break;
			
		case MOVEUPDATE:
			updateMove(Cam);
			break;
			
		case ATTACKSELECT:
			updateAttackSelect(Cam);
			break;
			
		case ATTACKUPDATE:
			updateAttack(Cam);
			break;
			
		default:
			break;
		}
		
		updateObjects(Cam);
		updateNullTanks(Cam);
		checkForDeaths(Cam);
		checkNextStage(Cam);
	}
	
	public void updateObjects(Camera Cam)
	{
		Iterator<FoxHole> f = foxholes.iterator();
		while (f.hasNext())
			f.next().update();
		
		Iterator<TankBarrier> b = barriers.iterator();
		while (b.hasNext())
			b.next().update();
	}
	
	public void updateNullTanks(Camera Cam)
	{
		Iterator<NullTank> t = nullTanks.iterator();
		while (t.hasNext())
			t.next().update(Cam);
	}
	
	public void checkForDeaths(Camera Cam)
	{
		userArmy.checkForDeaths(Cam, nullTanks, particles);
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().checkForDeaths(Cam, nullTanks, particles);
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().checkForDeaths(Cam, nullTanks, particles);
	}
	
	public void updateMoveSelect(Camera Cam)
	{
		userArmy.updateMoveSelect(Cam);
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().updateMoveSelect(Cam);
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().updateMoveSelect(Cam);
	}
	
	public void updateMove(Camera Cam)
	{
		userArmy.updateMove(Cam);
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().updateMove(Cam);
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().updateMove(Cam);
	}
	
	public void updateAttackSelect(Camera Cam)
	{
		userArmy.updateAttackSelect(Cam);
		if (userArmy.isTargeting()) {
			buildTargetStack(Cam, true);
		}
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext()) 
			f.next().updateAttackSelect(Cam);
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().updateAttackSelect(Cam);
	}
	
	private void updateAttack(Camera Cam)
	{
		// update the combat resolver
		resolver.updateSimulation();
	}
	
	public void procBlast(Blast B)
	{
		// process any blasts on all armies
		userArmy.procBlasts(B);
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().procBlasts(B);
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().procBlasts(B);
		
		procFoxBlast(B);
		
		ter.cutHole((int)B.pos.x, Game.WORLDH - (int)B.pos.y, (int)B.radius);
	}
	
	private void procFoxBlast(Blast B)
	{
		Iterator<FoxHole> f = foxholes.iterator();
		while (f.hasNext()) {
			
			FoxHole fox = f.next();
			if (Intersector.overlaps(new Circle(B.pos, B.radius), fox.getBBox()) || 
					fox.getBBox().contains(B.pos)) 
			{
				if (fox.isOccupied()) {
					fox.getOccupied().setUnoccupiedFox();
				}
				
				ter.addFoxHole((int)fox.getPos().x);
				f.remove();
			}
		}
	}
	
	private int getTargetSize(Camera Cam)
	{
		int count = 0;
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			count += e.next().getMouseOverCount(Cam);
		
		return count;
	}
	
	private void buildTargetStack(Camera Cam, boolean IgnoreFox)
	{
		// do not rebuilt the target stack when the stack size does not change
		if (!userArmy.updateTargetOptions( getTargetSize(Cam) ))
			return;
		
		// get all squad options from the mouse over
		SelectionStack stack = userArmy.getTargetOptions();
		stack.reset();
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext()) {
			e.next().getMouseOver(stack, Cam, IgnoreFox);
		}
	}
	
	private void drawTargets(SpriteBatch Batch, Camera Cam)
	{
		if (currentstage != MOVESELECT && currentstage != ATTACKSELECT)
			return;
		
		if (currentstage == MOVESELECT) {
			userArmy.drawTargetPos(Batch, Cam);
		} else if (currentstage == ATTACKSELECT) {
			userArmy.drawTargetSquad(Batch, Cam);
		}
	}
	
	private void drawFogMask(SpriteBatch Batch, Camera Cam)
	{
		FogOfWar.begin(Batch);
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().drawView(Cam);
		userArmy.drawView(Cam);
		
		FogOfWar.end(Batch);
	}
	
	private boolean checkTargets()
	{
		if (currentstage != ATTACKSELECT)
			return false;
		
		if (userArmy.isTargeting())
			return true;
		
		return false;
	}
	
	private void disableStencil(SpriteBatch Batch)
	{
		Batch.flush();
		Gdx.gl.glDisable(GL20.GL_STENCIL_TEST);
	}
	
	private void enableStencil(SpriteBatch Batch)
	{
		Batch.flush();
		Gdx.gl.glEnable(GL20.GL_STENCIL_TEST);
	}
	
	private void drawNullTanks(SpriteBatch Batch, Camera Cam)
	{
		Iterator<NullTank> t = nullTanks.iterator();
		while (t.hasNext()) 
			t.next().draw(Batch, Cam);
	}
	
	private void drawHidden(SpriteBatch Batch, Camera Cam)
	{
		// enable fog of war and draw the background
		FogOfWar.maskOn(Batch);
		Background.drawFG(Batch);
		Batch.flush();
		
		disableStencil(Batch);
		
		// draw the weather and the bases
		userArmy.drawBase(Batch, Cam);
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().drawBase(Batch, Cam);
		
		enableStencil(Batch);
		
		// draw the base logo's with the stencil test enabled
		userArmy.drawBaseLogo(Batch, Cam);
		e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().drawBaseLogo(Batch, Cam);
		
		disableStencil(Batch);
		
		/*
		 * Draw the terrain, weather and world objects.
		 */
		
		Weather.draw(Batch, Cam);
		ter.draw(Batch, Cam.getPos());
		
		Iterator<FoxHole> holes = foxholes.iterator();
		while (holes.hasNext())
			holes.next().render(Batch, Cam);
	
		drawNullTanks(Batch, Cam);
		/*
		 * Draw the terrain, weather and world objects.
		 */
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().drawBase(Batch, Cam);
		
		enableStencil(Batch);
		
		// draw the barricades
		Iterator<TankBarrier> barrIterator = barriers.iterator();
		while (barrIterator.hasNext())
			barrIterator.next().render(Batch, Cam);
		
		// draw all enemy units above the terrain, but hidden by the fog 
		e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().draw(Batch, Cam, checkTargets(), currentstage);
		
		FogOfWar.maskOff(Batch);
	}
	
	public void draw(SpriteBatch Batch, Camera Cam)
	{
		Background.drawBG(Batch);
		drawFogMask(Batch, Cam);
		drawHidden(Batch, Cam);
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().draw(Batch, Cam, false, currentstage);
		
		userArmy.draw(Batch, Cam, false, currentstage);
		drawTargets(Batch, Cam);
		
		if (currentstage == ATTACKUPDATE) {
			resolver.drawSimulation(Batch, Cam);
		}
		
		particles.draw(Batch, Cam);
		MenuBar.draw(Batch, Cam, currentstage, 
				(currentstage == MOVESELECT || currentstage == ATTACKSELECT) &&  !userArmy.isMenuOpen() && !userArmy.isStageCompleted(currentstage));
	}
	
	private boolean isArmiesStageCompleted()
	{
		Iterator<Army> f = friendlyArmy.iterator();
		Iterator<Army> e = enemyArmy.iterator();
		
		if (!userArmy.isStageCompleted(currentstage))
			return false;
		
		// loop through each army and check if any of them are not completed
		while (f.hasNext()) {
			if (!f.next().isStageCompleted(currentstage))
				return false;
		}

		while (e.hasNext()) {
			if (!e.next().isStageCompleted(currentstage))
				return false;
		}
		
		if (currentstage == ATTACKUPDATE && !resolver.isSimulationCompleted()) {
			return false;
		}
		
		return true;
	}
	
	private void initResolver()
	{
		if (currentstage != ATTACKUPDATE) {
			return;
		}
		
		resolver.startSimulation();
		Iterator<Army> f = friendlyArmy.iterator();
		Iterator<Army> e = enemyArmy.iterator();
		
		userArmy.addCombatData(resolver);
		while (f.hasNext()) {
			f.next().addCombatData(resolver);
		}
		
		while (e.hasNext()) {
			e.next().addCombatData(resolver);
		}
	}
	
	private void initNewStage(Camera Cam)
	{
		Iterator<Army> f = friendlyArmy.iterator();
		Iterator<Army> e = enemyArmy.iterator();

		userArmy.initStage(Cam, currentstage);
		
		while (f.hasNext())
			f.next().initStage(Cam, currentstage);

		while (e.hasNext())
			e.next().initStage(Cam, currentstage);
		
		initResolver();
	}
	
	public void checkNextStage(Camera Cam)
	{
		// if all the armies are ready to update update the current stage
		if ( !isArmiesStageCompleted() )
			return;
		
		// set the new stage
		currentstage++;
		if (currentstage == STAGECOUNT)
			currentstage = 0;
		
		initNewStage(Cam);
	}
	
	public static int getDirection(float StartX, float StartWidth, float TargetX, float TargetWidth)
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
	
	public static float getDistance(float StartX, float StartWidth, float TargetX, float TargetWidth)
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