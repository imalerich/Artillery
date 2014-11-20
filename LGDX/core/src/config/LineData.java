package config;

public class LineData 
{
	public final String param;
	public final String opt;
	
	/**
	 * Construct a new LineData Object.
	 * @param Param
	 * 	Parameter of the input.
	 * @param Opt
	 * 	Option for the parameter.
	 */
	public LineData(String Param, String Opt)
	{
		param = Param;
		opt = Opt;
	}
	
	/**
	 * Returns the Integer format of a string, if the input String is not a valid integer,
	 * the default value is returend.
	 * @param Filename
	 * 	Filename this line is from, used for error output.
	 * @param LineNumber
	 * 	Linenumber for this line, used for error output.
	 * @param Opt
	 * 	Option to be parsed.
	 * @param Default
	 * 	Default value to be used if an error occurs.
	 * @return
	 * 	An Integer parsed from Opt, if that fails, then Default is returned.
	 */
	public static int GetInt(String Filename, int LineNumber, String Opt, int Default)
	{
		try {
			return Integer.parseInt(Opt);
		} catch (NumberFormatException e) {
			PrintErr(Filename, LineNumber, Opt);
			System.err.println("Defaulting to a value of " + Default + '\n');
			return Default;
		}
	}
	
	/**
	 * Returns the Float format of a string, if the input String is not a valid floating point number,
	 * the default value is returend.
	 * @param Filename
	 * 	Filename this line is from, used for error output.
	 * @param LineNumber
	 * 	Linenumber for this line, used for error output.
	 * @param Opt
	 * 	Option to be parsed.
	 * @param Default
	 * 	Default value to be used if an error occurs.
	 * @return
	 * 	An Float parsed from Opt, if that fails, then Default is returned.
	 */
	public static float GetFloat(String Filename, int LineNumber, String Opt, float Default)
	{
		try {
			return Float.parseFloat(Opt);
		} catch (NumberFormatException e) {
			PrintErr(Filename, LineNumber, Opt);
			System.err.println("Defaulting to a value of " + Default + '\n');
			return Default;
		}
	}
	
	/**
	 * Output error information if an error occured while parsing an option.
	 * @param Filename
	 * 	Filename the error occured in.
	 * @param LineNumber
	 * 	Linenumber the error occured at.
	 * @param Opt
	 * 	The option that caused the error.
	 */
	private static void PrintErr(String Filename, int LineNumber, String Opt)
	{
		System.err.println("In file \'" + Filename + '\'');
		System.err.println("Error: Invalid Option found at Line " + LineNumber);
		System.err.println("\t--" + Opt + "--");
	}
}
