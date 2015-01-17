package config;

import java.util.Arrays;
import java.util.Scanner;

import arsenal.Armament;
import arsenal.Armor;

import com.badlogic.gdx.Gdx;

public class ConfigGenerator 
{
	/**
	 * Valid data tokens to be contained within a config file
	 */
	public static final String[] TOKENS = { "UNIT", "ARMOR", "PRIMARY", "SECONDARY", "END", "count:", "speed:", "health:",
		"strength:", "type:", "range:", "firerate:", "accuracy:", "movedist:", "reqcost:", "reqbonus:", "#" };
	
	/**
	 * Default count for a newly generate unit.
	 */
	public static final int DEFAULT_COUNT = 4;
	
	/**
	 * Default movement speed of the generated unit.
	 */
	public static final int DEFAULT_SPEED = 120;
	
	/**
	 * Default movement distance of the unit per turn;
	 */
	public static final int DEFAULT_MOVEDIST = 512;
	
	/**
	 * Default maximum health held by the unit.
	 */
	public static final int DEFAULT_HEALTH = 16;
	
	/**
	 * Default health for armor.
	 */
	public static final int DEFAULT_ARMOR_HEALTH = 0;
	
	/**
	 * Default strength for armor.
	 */
	public static final int DEFAULT_ARMOR_STRENGTH = 0;
	
	/**
	 * Default weapon type.
	 */
	public static final int DEFAULT_ARMAMENT_TYPE = Armament.UNITTARGET;
	
	/**
	 * Default weapon range.
	 */
	public static final int DEFAULT_ARMAMENT_RANGE = 512;
	
	/**
	 * Default weapon firerate.
	 */
	public static final int DEFAULT_ARMAMENT_FIRERATE = 1;
	
	/**
	 * Default weapon strength.
	 */
	public static final int DEFAULT_ARMAMENT_STRENGTH = 8;
	
	/**
	 * Default weapon speed.
	 */
	public static final int DEFAULT_ARMAMENT_SPEED = 800;
	
	/**
	 * Default weapon accuracy.
	 */
	public static final float DEFAULT_ARMAMENT_ACCURACY = 0.9f;
	
	/**
	 * Default requisition cost.
	 */
	public static final int DEFAULT_REQUISITION_COST = 400;
	
	/**
	 * Default requisition bonus;
	 */
	public static final int DEFAULT_REQUISITION_BONUS = 50;
	
	/**
	 * Load a configuration from the specified file.
	 * @param filename
	 * 	File be loaded.
	 * @return
	 * 	Configuration Settings described the file.
	 */
	public static ConfigSettings loadConfiguration(String Filename)
	{
		// load the files data into a string to be parsed
		String data = Gdx.files.internal(Filename).readString();
		
		validateFile(Filename, data);
		ConfigSettings c = initConfiguration(Filename, data);
		buildArmorStack(Filename, c, data);
		buildArmamentStack(Filename, c, data);
		
		return c;
	}
	
	/**
	 * Parse the input string for the default settings of the unit.
	 * @param Data
	 * 	Input configuration file.
	 * @return
	 * 	ConfigurationSettings with default data filled in, with no armor or
	 * 	armament added.
	 */
	private static ConfigSettings initConfiguration(String Filename, String Data)
	{
		int count = DEFAULT_COUNT;
		int speed = DEFAULT_SPEED;
		int health = DEFAULT_HEALTH;
		int movedist = DEFAULT_MOVEDIST;
		int reqcost = DEFAULT_REQUISITION_COST;
		int reqbonus = DEFAULT_REQUISITION_BONUS;
		
		boolean insegment = false;
		int linenumber = 0;
		
		Scanner s = new Scanner(Data);
		while (s.hasNext()) {
			linenumber++;
			LineData d = parseLine(s.nextLine());
			
			if (!isValidLine(d)) {
				continue;
			}
			
			// check whether or not we are in the segment or completed
			if (d.param.equals("UNIT")) {
				insegment = true;
				continue;
			} else if (d.param.equals("END") && insegment) {
				break;
			}
			
			// process parameter information
			if (d.param.equals("count:")) {
				count = LineData.getInt(Filename, linenumber, d.opt, DEFAULT_COUNT);
				continue;
			} else if (d.param.equals("speed:")) {
				speed = LineData.getInt(Filename, linenumber, d.opt, DEFAULT_SPEED);
				continue;
			} else if (d.param.equals("health:")) {
				health = LineData.getInt(Filename, linenumber, d.opt, DEFAULT_HEALTH);
				continue;
			} else if (d.param.equals("movedist:")) {
				movedist = LineData.getInt(Filename, linenumber, d.opt, DEFAULT_MOVEDIST);
				continue;
			} else if (d.param.equals("reqcost:")) {
				reqcost = LineData.getInt(Filename, linenumber, d.opt, DEFAULT_REQUISITION_COST);
				continue;
			} else if (d.param.equals("reqbonus:")) {
				reqbonus = LineData.getInt(Filename, linenumber, d.opt, DEFAULT_REQUISITION_BONUS);
				continue;
			}
		}
		
		// file processed succesfully, return the configuration setting
		return new ConfigSettings(count, health, speed, movedist, reqcost, reqbonus);
	}
	
	/**
	 * Parse the input string for armor data and add them to the configuration.
	 * @param Confg
	 * 	Configuration to add the data to.
	 * @param Data
	 * 	Data to be parsed.
	 */
	private static void buildArmorStack(String Filename, ConfigSettings Confg, String Data)
	{
		int health = DEFAULT_ARMOR_HEALTH;
		int strength = DEFAULT_ARMOR_STRENGTH;
		
		boolean insegment = false;
		int linenumber = 0;
		
		Scanner s = new Scanner(Data);
		while (s.hasNext()) {
			linenumber++;
			LineData d = parseLine(s.nextLine());
			
			if (!isValidLine(d)) {
				continue;
			}
			
			if (d.param.equals("ARMOR")) {
				insegment = true;
				continue;
			} else if (d.param.equals("END") && insegment) { 
				// end of ARMOR section - add the armor found and reset defaults
				insegment = false;
				Confg.addArmor( new Armor(health, strength) ); 
				
				health = DEFAULT_ARMOR_HEALTH;
				strength = DEFAULT_ARMOR_STRENGTH;
				continue;
			}
			
			// process parameter information
			if (d.param.equals("health:")) {
				health = LineData.getInt(Filename, linenumber, d.opt, DEFAULT_ARMOR_STRENGTH);
				continue;
			} else if (d.param.equals("strength:")) {
				strength = LineData.getInt(Filename, linenumber, d.opt, DEFAULT_ARMOR_STRENGTH);
				continue;
			}
		}
	}
	
	/**
	 * Prase the input string for armament data and add them to the configuration.
	 * @param Filename
	 * File name to be parsed.
	 * @param Confg
	 * Configuration to add the data to.
	 * @param Data
	 * Data to be parsed.
	 */
	private static void buildArmamentStack(String Filename, ConfigSettings Confg, String Data)
	{
		int type = DEFAULT_ARMAMENT_TYPE;
		int range = DEFAULT_ARMAMENT_RANGE;
		int firerate = DEFAULT_ARMAMENT_FIRERATE;
		int strength = DEFAULT_ARMAMENT_STRENGTH;
		int speed = DEFAULT_ARMAMENT_SPEED;
		float accuracy = DEFAULT_ARMAMENT_ACCURACY;
		
		boolean inprimary = false;
		boolean insegment = false;
		int linenumber = 0;
		
		Scanner s = new Scanner(Data);
		while (s.hasNext()) {
			linenumber++;
			LineData d = parseLine(s.nextLine());
			
			if (!isValidLine(d)) {
				continue;
			}
			
			if (d.param.equals("PRIMARY")) {
				insegment = true;
				inprimary = true;
				continue;
				
			} else if (d.param.equals("SECONDARY")) {
				insegment = true;
				inprimary = false;
				continue;
				
			} else if (d.param.equals("END") && insegment) { 
				// end of ARMOR section - add the armor found and reset defaults
				insegment = false;
				
				Armament a = new Armament(type, range, firerate, strength, speed, accuracy);
				if (inprimary) {
					Confg.addPrimary(a);
				} else {
					Confg.addSecondary(a);
				}
				
				type = DEFAULT_ARMAMENT_TYPE;
				range = DEFAULT_ARMAMENT_RANGE;
				firerate = DEFAULT_ARMAMENT_FIRERATE;
				strength = DEFAULT_ARMAMENT_STRENGTH;
				speed = DEFAULT_ARMAMENT_SPEED;
				accuracy = DEFAULT_ARMAMENT_ACCURACY;
				continue;
			}
			
			// process parameter information
			if (d.param.equals("type:")) {
				if (d.opt.equals("UNIT")) {
					type = Armament.UNITTARGET;
				} else if (d.opt.equals("POINT")) {
					type = Armament.POINTTARGET;
				}
				
				continue;
			} else if (d.param.equals("range:")) {
				range = LineData.getInt(Filename, linenumber, d.opt, DEFAULT_ARMAMENT_RANGE);
				continue;
			} else if (d.param.equals("firerate:")) {
				firerate = LineData.getInt(Filename, linenumber, d.opt, DEFAULT_ARMAMENT_FIRERATE);
				continue;
			} else if (d.param.equals("strength:")) {
				strength = LineData.getInt(Filename, linenumber, d.opt, DEFAULT_ARMAMENT_STRENGTH);
				continue;
			} else if (d.param.equals("speed:")) {
				speed = LineData.getInt(Filename, linenumber, d.opt, DEFAULT_ARMAMENT_SPEED);
				continue;
			} else if (d.param.equals("accuracy:")) {
				accuracy = LineData.getFloat(Filename, linenumber, d.opt, DEFAULT_ARMAMENT_ACCURACY);
				continue;
			}
		}
	}
	
	/**
	 * Check whether or not the given LineData object should be parsed.
	 * @param D
	 * 	Given LineData object.
	 * @return
	 * 	Whether or not the object should be parsed.
	 */
	private static boolean isValidLine(LineData D)
	{
		// check if the line is a comment or a blank line
		if (D == null) {
			return false;
		} else if (D.param.length() == 0) {
			return false;
		} if (D.param.equals("#")) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Process the input files data and check for any invalid tokens.
	 * @param Filename
	 * 	Input filename.
	 * @param Data
	 * 	Input data.
	 */
	private static void validateFile(String Filename, String Data)
	{
		int linenumber = 0;
		Scanner s = new Scanner(Data);
		while (s.hasNextLine()) {
			linenumber++;
			LineData d = parseLine(s.nextLine());
			if (!isValidLine(d)) {
				continue;
			}
			
			checkParam(Filename, d.param, linenumber);
		}
	}
	
	/**
	 * Returns whether or not the given token is a valid option.
	 * @param T
	 * 	Token to be parsed.
	 * @return
	 *  Whether or not the given token is valid.
	 */
	private static void checkParam(String Filename, String T, int LineNumber)
	{
		Arrays.sort(TOKENS);
		if (Arrays.binarySearch(TOKENS, T) < 0) {
			System.err.println("In file \'" + Filename + '\'');
			System.err.println("Error: Invalid option found at Line " + LineNumber);
			System.err.println("\t--" + T + "--");
			System.err.println("Ignoring\n");
		}
	}
	
	/**
	 * Parse a line of text, and return its parameter and options.
	 * @param Line
	 * 	Line of text to be parsed.
	 * @return
	 * 	LineData to contained by the line of text.
	 */
	private static LineData parseLine(String Line)
	{
		Scanner s = new Scanner(Line);
		
		if (!s.hasNext()) {
			return null;
		}
		
		String param = s.next();
		
		if (!s.hasNext()) {
			return new LineData(param, "");
		}
		
		return new LineData(param, s.next());
	}
}
