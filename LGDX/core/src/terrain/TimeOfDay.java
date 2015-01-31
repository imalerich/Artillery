package terrain;

import com.badlogic.gdx.Gdx;

public class TimeOfDay 
{
	public static float DAYLENGTH = 8f;
	public static float NIGHTLENGTH = 8f;
	public static float TRANSTIME = 3f;
	public static float TOTALTIME = DAYLENGTH + NIGHTLENGTH + TRANSTIME*2f;
	
	private static float time = 0f;
	
	public static void update()
	{
		time += Gdx.graphics.getDeltaTime();
		if (time > TOTALTIME)
			time -= TOTALTIME;
	}
	
	public static float getTime()
	{
		return time;
	}
	
	public static boolean isDay()
	{
		return time < DAYLENGTH;
	}
	
	public static boolean isNight()
	{
		return (time > DAYLENGTH + TRANSTIME) && (time < TOTALTIME-TRANSTIME);
	}
	
	public static boolean isTrans()
	{
		return (!isDay() && !isNight());
	}
	
	public static float getTrans()
	{
		if (!isTrans())
			return 0f;
		
		if (time > DAYLENGTH && time < DAYLENGTH + TRANSTIME)
			return -(time - DAYLENGTH)/3f + 1f;
		else
			return (time - (TOTALTIME-TRANSTIME))/3f;
	}
}
