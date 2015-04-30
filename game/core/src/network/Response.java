package network;


public class Response 
{
	// generic data to be sent
	public String request;
	public Integer source = -1;
	public Integer dest = -1;
	public Integer army = -1;
	public Integer squad = -1;
	
	public Integer i0;
	public Integer i1;
	public Integer i2;
	
	public Boolean b0;
	public Boolean b1;
	public Boolean b2;
	
	public float f0;
	public float f1;
	public float f2;
	
	// information on armament specifications for use when units fire 
	public float armsFirerate;
	public int armsStrength;
	public int armsSpeed;
	
	public int armsBounces;
	public int armsDivcount;
	public int armsBreakcount;
	public boolean armsIncinerate;
}
