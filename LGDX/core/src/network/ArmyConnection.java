package network;

public class ArmyConnection 
{
	public Integer pos;
	public Integer tankoff;
	public Integer id;
	
	public ArmyConnection()
	{
		//
	}
	
	public ArmyConnection(ArmyConnection A)
	{
		pos = A.pos;
		tankoff = A.tankoff;
		id = A.id;
	}
}
