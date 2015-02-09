package ui;

import arsenal.Armament;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ProfileTankOptions 
{
	public static void draw(SpriteBatch Batch, Armament A, int YPos, int Offset)
	{
		ProfileTankOptionButton.setPos(0, YPos, 5, Offset);
		if (ProfileTankOptionButton.isActive())
			setBounces(A);
		ProfileTankOptionButton.draw(Batch, A.getBounces() > 0, 0);

		ProfileTankOptionButton.setPos(2+ProfileTankOptionButton.getWidth(), YPos, 7, Offset);
		if (ProfileTankOptionButton.isActive())
			setDivCount(A);
		ProfileTankOptionButton.draw(Batch, A.getDivCount() > 0, 1);
	}
	
	private static void setBounces(Armament A)
	{
		if (A.getBounces() == 0)
			A.setBounces(3);
		else
			A.setBounces(0);
	}
	
	private static void setDivCount(Armament A)
	{
		if (A.getDivCount() == 0)
			A.setDivCount(8);
		else
			A.setDivCount(0);
	}
}
