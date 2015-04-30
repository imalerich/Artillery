package config;

import com.badlogic.gdx.Audio;

public class GameConfigs 
{
	public final boolean AUDIOENABLED;
	public final int WORLDWIDTH;
	public final int WORLDHEIGHT;
	public final float CAMPANSPEED;
	public final int OUTPOSTSPERSIDE;
	
	public GameConfigs(boolean AudioEnabled, int WorldWidth, int WorldHeight, float CamPanSpeed, int OutpostsPerSide) 
	{
		AUDIOENABLED = AudioEnabled;
		WORLDWIDTH = WorldWidth;
		WORLDHEIGHT = WorldHeight;
		CAMPANSPEED = CamPanSpeed;
		OUTPOSTSPERSIDE = OutpostsPerSide;
	}
}
