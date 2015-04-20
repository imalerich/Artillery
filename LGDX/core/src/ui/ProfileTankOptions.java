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
		ProfileTankOptionButton.draw(Batch, A.getBounces() > 0, A.bouncesUnlocked ? 0 : 4);

		ProfileTankOptionButton.setPos(2+ProfileTankOptionButton.getWidth(), YPos, 7, Offset);
		if (ProfileTankOptionButton.isActive())
			setDivCount(A);
		ProfileTankOptionButton.draw(Batch, A.getDivCount() > 0, A.divUnlocked ? 1 : 4);

		ProfileTankOptionButton.setPos(4+2*ProfileTankOptionButton.getWidth(), YPos, 7, Offset);
		if (ProfileTankOptionButton.isActive())
			setBreakCount(A);
		ProfileTankOptionButton.draw(Batch, A.getBreakCount() > 0, A.breakUnlocked ? 2 : 4);

		ProfileTankOptionButton.setPos(6+3*ProfileTankOptionButton.getWidth(), YPos, 7, Offset);
		if (ProfileTankOptionButton.isActive())
			setIncinerate(A);
		ProfileTankOptionButton.draw(Batch, A.doIncinerate(), A.incinerateUnlocked ? 3 : 4);
	}
	
	private static void setBounces(Armament A)
	{
		if (A.getBounces() == 0 && A.bouncesUnlocked)
			A.setBounces(3);
		else
			A.setBounces(0);
	}
	
	private static void setDivCount(Armament A)
	{
		if (A.getDivCount() == 0 && A.divUnlocked) {
			A.setDivCount(8);
			A.setBreakCount(0);
			A.setIncinerate(false);
		} else
			A.setDivCount(0);
	}
	
	private static void setBreakCount(Armament A)
	{
		if (A.getBreakCount() == 0 && A.breakUnlocked) {
			A.setBreakCount(8);
			A.setDivCount(0);
			A.setIncinerate(false);
		} else
			A.setBreakCount(0);
	}
	
	private static void setIncinerate(Armament A)
	{
		if (A.doIncinerate() == false && A.incinerateUnlocked) {
			A.setIncinerate(true);
			A.setDivCount(0);
			A.setBreakCount(0);
		} else
			A.setIncinerate(false);
	}
}
