package entity;

import java.util.Vector;

import terrain.Terrain;

import com.mygdx.game.MilitaryBase;

public class RemoteArmy extends Army
{
	public RemoteArmy(MilitaryBase Base, Terrain Ter)
	{
		ter = Ter;
		base = Base;
		squads = new Vector<Squad>();
	}
}
