package config;

import com.badlogic.gdx.Gdx;

public class AppConfigs 
{
	private static final String FILENAME = "config/artillery.rc";
	public static ArmsConfigs Arms;
	
	public static void init() 
	{
		// load the files data into a string to be parsed
		String data = Gdx.files.internal(FILENAME).readString();

		AppConfigGenerator.validateFile(FILENAME, data);
		Arms = AppConfigGenerator.loadArmsConfiguration(FILENAME, data);
	}
}
