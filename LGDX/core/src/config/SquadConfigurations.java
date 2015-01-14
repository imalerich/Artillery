package config;

public class SquadConfigurations 
{
	private static final int CONFIGCOUNT = 5;
	public static final int GUNMAN = 0;
	public static final int SPECOPS = 1;
	public static final int STEALTHOPS = 2;
	public static final int TANK = 3;
	public static final int TOWER = 4;
	
	private static ConfigSettings[] settings;
	
	public static void init()
	{
		settings = new ConfigSettings[CONFIGCOUNT];
		settings[GUNMAN] = ConfigGenerator.loadConfiguration("config/gunman.rc");
		settings[SPECOPS] = ConfigGenerator.loadConfiguration("config/specops.rc");
		settings[STEALTHOPS] = ConfigGenerator.loadConfiguration("config/stealthops.rc");
		settings[TANK] = ConfigGenerator.loadConfiguration("config/tank.rc");
		settings[TOWER] = ConfigGenerator.loadConfiguration("config/tower.rc");
	}
	
	public static ConfigSettings getConfiguration(int Index)
	{
		if (!isValidConfig(Index)) {
			System.err.println("Invalid Configuration Specified at Index " + Index);
			return null;
		}
		
		return settings[Index];
	}
	
	private static boolean isValidConfig(int Index)
	{
		return (Index >= 0) && (Index < CONFIGCOUNT);
	}
}
