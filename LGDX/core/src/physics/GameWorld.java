package physics;

import java.util.Iterator;
import java.util.Vector;

import network.Request;
import objects.FoxHole;
import objects.TankBarrier;
import particles.Particles;
import particles.Weather;
import terrain.Background;
import terrain.FogOfWar;
import terrain.Terrain;
import ui.MenuBar;
import ui.OutpostFlag;
import audio.AudioWorld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Camera;
import com.mygdx.game.Cursor;
import com.mygdx.game.Game;

import entity.Army;
import entity.SelectionStack;
import entity.UserArmy;

public class GameWorld 
{
	public static final int STAGECOUNT = 4;
	public static final int MOVESELECT = 0;
	public static final int MOVEUPDATE = 1;
	public static final int ATTACKSELECT = 2;
	public static final int ATTACKUPDATE = 3;
	
	public static final int CAMSPEED = 512;
	
	private int currentstage;
	private int nextturn = -1;
	
	private Camera cam;
	private CombatResolver resolver;
	
	private Terrain ter;
	private Particles particles;
	private AudioWorld audio;
	
	private Army currentTurn;
	private UserArmy userArmy;
	private Vector<Army> friendlyArmy;
	private Vector<Army> enemyArmy;
	
	private Vector<OutpostFlag> markers;
	
	private Vector<NullTank> nullTanks;
	private Vector<FoxHole> foxholes;
	private Vector<TankBarrier> barriers;
	private Vector<Blast> blasts;
	
	public GameWorld(Terrain Ter)
	{
		ter = Ter;
		
		currentstage = MOVESELECT;
		particles = new Particles();
		resolver = new CombatResolver(this, ter, particles);
		audio = new AudioWorld(this);
		
		userArmy = null;
		currentTurn = null;
		
		friendlyArmy	= new Vector<Army>();
		enemyArmy		= new Vector<Army>();
		
		markers			= new Vector<OutpostFlag>();
		nullTanks		= new Vector<NullTank>();
		foxholes		= new Vector<FoxHole>();
		barriers		= new Vector<TankBarrier>();
		blasts 			= new Vector<Blast>();
		
		// create the camera
		cam = new Camera();
		cam.setWorldMin( new Vector2(0.0f, 0.0f) );
		cam.setWorldMax( new Vector2(Game.WORLDW, Game.WORLDH) );
		cam.setPos( new Vector2(0, ter.getHeight(0) - Game.SCREENH/2) );
	}
	
	public void release()
	{
		ter.release();
	}
	
	public AudioWorld getAudio()
	{
		return audio;
	}
	
	public Camera getCam()
	{
		return cam;
	}
	
	public Terrain getTerrain()
	{
		return ter;
	}
	
	public void setCurrentTurn(int ID)
	{
		// next turn received 
		nextturn = ID;
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
	
	public void forceStage(int Stage)
	{
		currentstage = Stage;
	}
	
	public int getTurn()
	{
		if (currentTurn == null)
			return -1;
		
		return currentTurn.getConnection();
	}
	
	public void setUserArmy(UserArmy Add)
	{
		userArmy = Add;
	}
	
	public void requestFirstTurn()
	{
		if (userArmy == null) {
			System.err.print("Error: cannot request turn when user army is not set.");
		}
		
		// request the first turn for the game
		Request r = new Request();
		r.source = userArmy.getConnection();
		r.req = "FirstTurn";
		r.i0 = 0;

		userArmy.getNetwork().getClient().sendTCP(r);
	}
	
	public void addOutpostMarker(int XPos, int ID)
	{
		markers.add( new OutpostFlag(XPos, ter, ID) );
	}
	
	public OutpostFlag getMarker(int ID)
	{
		Iterator<OutpostFlag> i = markers.iterator();
		while (i.hasNext()) {
			OutpostFlag f = i.next();
			if (f.getId() == ID) {
				return f;
			}
		}
		
		return null;
	}
	
	public void removeOutpostMarker(int ID)
	{
		Iterator<OutpostFlag> i = markers.iterator();
		while (i.hasNext()) {
			if (i.next().getId() == ID) {
				i.remove();
				return;
			}
		}
		
		System.err.println("The Speicified marker at " + ID + " could not be found for removal, continuing normally.");
	}
	
	public void addFriendlyArmy(Army Add)
	{
		friendlyArmy.add(Add);
	}
	
	public void addEnemyArmy(Army Add)
	{
		enemyArmy.add(Add);
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
	
	public void updateThreads()
	{
		// synchronize data between threads
		userArmy.updateThreads(cam);
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().updateThreads(cam);
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().updateThreads(cam);
	}
	
	public void update()
	{
		updateCam();
		ter.update();
		particles.update();
		
		switch (currentstage)
		{
		case MOVESELECT:
			updateMoveSelect();
			break;
			
		case MOVEUPDATE:
			updateMove();
			break;
			
		case ATTACKSELECT:
			updateAttackSelect();
			break;
			
		case ATTACKUPDATE:
			updateAttack();
			break;
			
		default:
			break;
		}
		
		updateObjects();
		checkForDeaths();
		checkNextStage();
		updateThreads();
	}
	
	private void updateCam()
	{
		cam.update();
		
		// move the camera with the mouse
		if (Cursor.isButtonPressed(Cursor.MIDDLE))
		{
			cam.moveHorizontal( 6 * -Cursor.getDeltaX() );
			cam.moveVertical( 6 * Cursor.getDeltaY() );
		}
		
		// move the camera with the keyboard
		if (Gdx.input.isKeyPressed(Keys.D))
			cam.moveHorizontal( CAMSPEED * Gdx.graphics.getDeltaTime() );
		else if (Gdx.input.isKeyPressed(Keys.A))
			cam.moveHorizontal( -CAMSPEED * Gdx.graphics.getDeltaTime() );
		
		if (Gdx.input.isKeyPressed(Keys.W))
			cam.moveVertical( CAMSPEED * Gdx.graphics.getDeltaTime() );
		else if (Gdx.input.isKeyPressed(Keys.S))
			cam.moveVertical( -CAMSPEED * Gdx.graphics.getDeltaTime() );
	}
	
	public void updateObjects()
	{
		updateNullTanks();
		
		Iterator<FoxHole> f = foxholes.iterator();
		while (f.hasNext())
			f.next().update();
		
		Iterator<TankBarrier> b = barriers.iterator();
		while (b.hasNext())
			b.next().update();
		
		Iterator<Blast> bl = blasts.iterator();
		while (bl.hasNext()) {
			Blast blast = bl.next();
			blast.update();
			
			if (!blast.isAlive())
				bl.remove();
		}
	}
	
	public void updateNullTanks()
	{
		Iterator<NullTank> t = nullTanks.iterator();
		while (t.hasNext())
			t.next().update(cam);
	}
	
	public void checkForDeaths()
	{
		userArmy.checkForDeaths(cam, nullTanks, particles);
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().checkForDeaths(cam, nullTanks, particles);
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().checkForDeaths(cam, nullTanks, particles);
	}
	
	public void updateMoveSelect()
	{
		if (currentTurn == null) {
			return;
		}
		
		currentTurn.updateMoveSelect(cam);
	}
	
	public void updateMove()
	{
		userArmy.updateMove(cam);
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().updateMove(cam);
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().updateMove(cam);
	}
	
	public void updateAttackSelect()
	{
		if (currentTurn == null) {
			return;
		}
		
		currentTurn.updateAttackSelect(cam);
		if (currentTurn == userArmy && userArmy.isTargeting()) {
			buildTargetStack(true);
		}
	}
	
	private void updateAttack()
	{
		// update the combat resolver
		resolver.updateSimulation(cam);
	}
	
	public void procBlast(Blast B)
	{
		blasts.add(B);
		
		cam.addShakeIntensity(B.radius/16f);
		
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
	
	private int getTargetSize()
	{
		int count = 0;
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			count += e.next().getMouseOverCount(cam);
		
		return count;
	}
	
	private void buildTargetStack(boolean IgnoreFox)
	{
		// do not rebuilt the target stack when the stack size does not change
		if (!userArmy.updateTargetOptions( getTargetSize() ))
			return;
		
		// get all squad options from the mouse over
		SelectionStack stack = userArmy.getTargetOptions();
		stack.reset();
		
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext()) {
			e.next().getMouseOver(stack, cam, IgnoreFox);
		}
	}
	
	private void drawTargets(SpriteBatch Batch)
	{
		if (currentstage != MOVESELECT && currentstage != ATTACKSELECT)
			return;
		
		if (currentstage == MOVESELECT) {
			userArmy.drawTargetPos(Batch, cam);
		} else if (currentstage == ATTACKSELECT) {
			userArmy.drawTargetSquad(Batch, cam);
		}
	}
	
	private void drawFogMask(SpriteBatch Batch)
	{
		FogOfWar.begin(Batch);
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().drawView(cam);
		userArmy.drawView(cam);
		
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
	
	public OutpostFlag getFlagOver()
	{
		Iterator<OutpostFlag> m = markers.iterator();
		while (m.hasNext()) {
			OutpostFlag f = m.next();
			if (Cursor.isMouseOver(f.getBBox(), cam.getPos()))
				return f;
		}
		
		return null;
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
	
	private void drawNullTanks(SpriteBatch Batch)
	{
		Iterator<NullTank> t = nullTanks.iterator();
		while (t.hasNext()) 
			t.next().draw(Batch, cam);
	}
	
	private void drawMarkers(SpriteBatch Batch)
	{
		Iterator<OutpostFlag> m = markers.iterator();
		while (m.hasNext())
			m.next().draw(Batch, cam, userArmy.checkOutpostFlags());
	}
	
	private void drawHidden(SpriteBatch Batch)
	{
		// enable fog of war and draw the background
		FogOfWar.maskOn(Batch);
		Background.drawFG(Batch);
		Batch.flush();
		
		disableStencil(Batch);
		
		// draw the weather and the bases
		userArmy.drawBase(Batch, cam);
		Iterator<Army> e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().drawBase(Batch, cam);
		
		enableStencil(Batch);
		
		// draw the base logo's with the stencil test enabled
		userArmy.drawBaseLogo(Batch, cam);
		e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().drawBaseLogo(Batch, cam);
		
		disableStencil(Batch);
		
		/*
		 * Draw the terrain, weather and world objects.
		 */
		
		Weather.draw(Batch, cam);
		ter.draw(Batch, cam.getPos());
		
		Iterator<FoxHole> holes = foxholes.iterator();
		while (holes.hasNext())
			holes.next().render(Batch, cam);
	
		drawNullTanks(Batch);
		
		/*
		 * Draw the terrain, weather and world objects.
		 */
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().drawBase(Batch, cam);
		
		enableStencil(Batch);
		
		drawMarkers(Batch);
		
		// draw the barricades
		Iterator<TankBarrier> barrIterator = barriers.iterator();
		while (barrIterator.hasNext())
			barrIterator.next().render(Batch, cam);
		
		// draw all enemy units above the terrain, but hidden by the fog 
		e = enemyArmy.iterator();
		while (e.hasNext())
			e.next().draw(Batch, cam, checkTargets(), currentstage);
		
		FogOfWar.maskOff(Batch);
	}
	
	public void draw(SpriteBatch Batch)
	{
		Background.drawBG(Batch);
		drawFogMask(Batch);
		drawHidden(Batch);
		
		Iterator<Army> f = friendlyArmy.iterator();
		while (f.hasNext())
			f.next().draw(Batch, cam, false, currentstage);
		
		userArmy.draw(Batch, cam, false, currentstage);
		drawTargets(Batch);
		
		if (currentstage == ATTACKUPDATE) {
			resolver.drawSimulation(Batch, cam);
		}
		
		particles.draw(Batch, cam);
		
		Blast.begin(Batch);
		Iterator<Blast> b = blasts.iterator();
		while (b.hasNext())
			b.next().draw(cam);
		Blast.end(Batch);
		
		if (currentTurn == userArmy) {
			MenuBar.setUsersTurn(true);
		} else {
			MenuBar.setUsersTurn(false);
		}
		
		MenuBar.draw(Batch, cam, currentstage, (currentTurn == userArmy) && 
				(currentstage == MOVESELECT || currentstage == ATTACKSELECT) &&  !userArmy.isMenuOpen() && !userArmy.isStageCompleted(currentstage));
	}
	
	private boolean isArmiesStageCompleted()
	{
		if (currentTurn == null) {
			return false;
		}
		
		if (!currentTurn.isStageCompleted(currentstage)) {
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
		if (currentTurn == null) {
			return;
		}
		
		currentTurn.addCombatData(resolver);
	}
	
	private void initNewStage()
	{
		if (currentTurn == null) {
			return;
		}
		
		currentTurn.initStage(cam, currentstage);
	}
	
	public void checkNextStage()
	{
		// if there currently is not turn set, initialize a new turn
		if (currentTurn == null && nextturn != -1) {
			currentTurn = getRemoteArmy(nextturn);
			currentstage = 0;
			nextturn = -1;
			initNewStage();
		}
		
		// if all the armies are ready to update update the current stage
		if ( !isArmiesStageCompleted() )
			return;
	
		// set the new stage
		currentstage++;
		if (currentstage == STAGECOUNT) {
			currentstage = 0;
			
			// request for the next turn
			if (currentTurn == userArmy) {
				Request r = new Request();
				r.source = userArmy.getConnection();
				r.req = "NextTurn";

				userArmy.getNetwork().getClient().sendTCP(r);
			}
			
			currentTurn = null;
		}
		
		initNewStage();
		initResolver();
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