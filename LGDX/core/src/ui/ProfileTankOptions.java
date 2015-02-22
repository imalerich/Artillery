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

		ProfileTankOptionButton.setPos(4+2*ProfileTankOptionButton.getWidth(), YPos, 7, Offset);
		if (ProfileTankOptionButton.isActive())
			setBreakCount(A);
		ProfileTankOptionButton.draw(Batch, A.getBreakCount() > 0, 2);

		ProfileTankOptionButton.setPos(6+3*ProfileTankOptionButton.getWidth(), YPos, 7, Offset);
		if (ProfileTankOptionButton.isActive())
			setIncinerate(A);
		ProfileTankOptionButton.draw(Batch, A.doIncinerate(), 3);
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
	
	private static void setBreakCount(Armament A)
	{
		if (A.getBreakCount() == 0)
			A.setBreakCount(8);
		else
			A.setBreakCount(0);
	}
	
	private static void setIncinerate(Armament A)
	{
		if (A.doIncinerate() == false)
			A.setIncinerate(true);
		else
			A.setIncinerate(false);
	}
}
