package config;

public class ArmsConfigs 
{
	// armament properties
	public final int BOUNCECOST;
	public final int INCINERATECOST;
	public final int DIVCOST;
	public final int BREAKCOST;
	
	public ArmsConfigs(int BounceCost, int IncinerateCost, int DivCost, int BreakCost)
	{
		BOUNCECOST = BounceCost;
		INCINERATECOST = IncinerateCost;
		DIVCOST = DivCost;
		BREAKCOST = BreakCost;
	}
}
