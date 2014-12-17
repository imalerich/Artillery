package audio;

import physics.GameWorld;

import com.badlogic.gdx.audio.Sound;
import com.mygdx.game.Game;

public class AudioWorld 
{
	private final GameWorld world;
	
	public AudioWorld(GameWorld World)
	{
		world = World;
	}
	
	public void playSound(Sound Sfx, float XPos)
	{
		float vol = getVol(XPos);
		float pan = getPan(XPos);
		
		Sfx.play(vol, 1, pan);
	}
	
	public void playSound(Sound Sfx, float XPos, float Vol)
	{
		float vol = getVol(XPos);
		float pan = getPan(XPos);
		
		Sfx.play(vol*Vol, 1, pan);
	}
	
	private float getPan(float X)
	{
		float campos = world.getCam().getPos().x + Game.SCREENW/2f;
		float r = getRightDist(campos, X);
		float l = getLeftDist(campos, X);
		
		if (r < l) {
			return Math.min( Math.max(2f * r/Game.WORLDW, 0f), 1f);
		} else {
			return -Math.min( Math.max(2f * l/Game.WORLDW, 0f), 1f);
		}
	}
	
	private float getVol(float X)
	{
		float campos = world.getCam().getPos().x + Game.SCREENW/2f;
		float dist = getDist(campos, X);
		
		return 1f - Math.min( Math.max(2f * dist/Game.WORLDW, 0f), 1f);
	}
	
	private float getDist(float Start, float End)
	{
		return Math.min(getLeftDist(Start, End), getRightDist(Start, End));
	}
	
	private float getLeftDist(float Start, float End)
	{
		float ldist = Start + (Game.WORLDW - End);
		if (End< Start)
			ldist = (Start - End);
		
		return ldist;
	}
	
	private float getRightDist(float Start, float End)
	{
		float rdist = (Game.WORLDW-(Start)) + End;
		if (End > Start)
			rdist = End - (Start);

		return rdist;
	}
}
