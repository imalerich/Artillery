package config;

import java.util.Arrays;
import java.util.Scanner;

public class AppConfigGenerator 
{
	/**
	 * Valid data tokens to be contained within a config file
	 */
	public static final String[] TOKENS = { "ARMS", "END", "incinerate_cost:", "bounce_cost:", "div_cost:", "break_cost:", "#" };
	
	// defaults
	public static final int DEFAULT_ARMS_BOUNCE_COST = 500;
	public static final int DEFAULT_ARMS_INCINERATE_COST = 500;
	public static final int DEFAULT_ARMS_DIV_COST = 500;
	public static final int DEFAULT_ARMS_BREAK_COST = 500;
	
	/**
	 * Parse the input string for the default settings of the unit.
	 * @param Data
	 * 	Input configuration file.
	 * @return
	 * 	ConfigurationSettings with default data filled in, with no armor or
	 * 	armament added.
	 */
	public static ArmsConfigs loadArmsConfiguration(String Filename, String Data)
	{
		int arms_bounce_cost = DEFAULT_ARMS_BOUNCE_COST;
		int arms_incinerate_cost = DEFAULT_ARMS_INCINERATE_COST;
		int arms_div_cost = DEFAULT_ARMS_DIV_COST;
		int arms_break_cost = DEFAULT_ARMS_BREAK_COST;
		
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
			if (d.param.equals("ARMS")) {
				insegment = true;
				continue;
			} else if (d.param.equals("END") && insegment) {
				break;
			}
			
			// process parameter information
			if (d.param.equals("bounce_cost:")) {
				arms_bounce_cost = LineData.getInt(Filename, linenumber, d.opt, DEFAULT_ARMS_BOUNCE_COST);
				continue;
			} else if (d.param.equals("incinerate_cost:")) {
				arms_incinerate_cost = LineData.getInt(Filename, linenumber, d.opt, DEFAULT_ARMS_INCINERATE_COST);
				continue;
			} else if (d.param.equals("div_cost:")) {
				arms_div_cost = LineData.getInt(Filename, linenumber, d.opt, DEFAULT_ARMS_DIV_COST);
				continue;
			} else if (d.param.equals("break_cost:")) {
				arms_break_cost = LineData.getInt(Filename, linenumber, d.opt, DEFAULT_ARMS_BREAK_COST);
				continue;
			}
		}
		
		// file processed succesfully, return the configuration setting
		return new ArmsConfigs(arms_bounce_cost, arms_incinerate_cost, arms_div_cost, arms_break_cost);
	}
	
	/**
	 * Process the input files data and check for any invalid tokens.
	 * @param Filename
	 * 	Input filename.
	 * @param Data
	 * 	Input data.
	 */
	public static void validateFile(String Filename, String Data)
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
}
