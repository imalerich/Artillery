package config;

import com.badlogic.gdx.Audio;

public class GameConfigs 
{
	public final boolean AUDIOENABLED;
	public final int WORLDWIDTH;
	public final int WORLDHEIGHT;
	public final float CAMPANSPEED;
	
	public GameConfigs(boolean AudioEnabled, int WorldWidth, int WorldHeight, float CamPanSpeed) 
	{
		AUDIOENABLED = AudioEnabled;
		WORLDWIDTH = WorldWidth;
		WORLDHEIGHT = WorldHeight;
		CAMPANSPEED = CamPanSpeed;
	}
}
