package config;

public class SquadConfigurations 
{
	private static final int CONFIGCOUNT = 4;
	public static final int GUNMAN = 0;
	public static final int SPECOPS = 1;
	public static final int STEALTHOPS = 2;
	public static final int TANK = 3;
	
	private static ConfigSettings[] settings;
	
	public static void Init()
	{
		settings = new ConfigSettings[CONFIGCOUNT];
		settings[GUNMAN] = ConfigGenerator.LoadConfiguration("config/gunman.rc");
		settings[SPECOPS] = ConfigGenerator.LoadConfiguration("config/specops.rc");
		settings[STEALTHOPS] = ConfigGenerator.LoadConfiguration("config/stealthops.rc");
		settings[TANK] = ConfigGenerator.LoadConfiguration("config/tank.rc");
	}
	
	public static ConfigSettings GetConfiguration(int Index)
	{
		if (!IsValidConfig(Index)) {
			System.err.println("Invalid Configuration Specified");
		}
		
		return settings[Index];
	}
	
	private static boolean IsValidConfig(int Index)
	{
		return (Index > 0) && (Index < CONFIGCOUNT);
	}
}
