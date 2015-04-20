package ui;

import arsenal.Armament;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import config.AppConfigs;
import entity.Squad;

public class ProfileTankOptions 
{
	public static void draw(SpriteBatch Batch, Squad S, Armament A, int YPos, int Offset)
	{
		// always draw the first button, will display as a locked symbol if unavailable (must be unlocked in order)
		ProfileTankOptionButton.setPos(0, YPos, 5, Offset);
		if (ProfileTankOptionButton.isMouseOver())
			MenuBar.setTmpRequisition(S.getArmy().getReq() - AppConfigs.Arms.BOUNCECOST);
		if (ProfileTankOptionButton.isActive())
			setBounces(A, S);
		ProfileTankOptionButton.draw(Batch, A.getBounces() > 0, A.bouncesUnlocked ? 0 : 4);

		// if the user has the previous enhancement unlocked, allow them to unlock the next one
		if (A.bouncesUnlocked) {
			ProfileTankOptionButton.setPos(2+1*ProfileTankOptionButton.getWidth(), YPos, 7, Offset);
			if (ProfileTankOptionButton.isMouseOver())
				MenuBar.setTmpRequisition(S.getArmy().getReq() - AppConfigs.Arms.INCINERATECOST);
			if (ProfileTankOptionButton.isActive())
				setIncinerate(A, S);
			ProfileTankOptionButton.draw(Batch, A.doIncinerate(), A.incinerateUnlocked ? 3 : 4);
		}

		if (A.incinerateUnlocked) {
			ProfileTankOptionButton.setPos(4+2*ProfileTankOptionButton.getWidth(), YPos, 7, Offset);
			if (ProfileTankOptionButton.isMouseOver())
				MenuBar.setTmpRequisition(S.getArmy().getReq() - AppConfigs.Arms.DIVCOST);
			if (ProfileTankOptionButton.isActive())
				setDivCount(A, S);
			ProfileTankOptionButton.draw(Batch, A.getDivCount() > 0, A.divUnlocked ? 1 : 4);
		}

		if (A.divUnlocked) {
			ProfileTankOptionButton.setPos(6+3*ProfileTankOptionButton.getWidth(), YPos, 7, Offset);
			if (ProfileTankOptionButton.isMouseOver())
				MenuBar.setTmpRequisition(S.getArmy().getReq() - AppConfigs.Arms.BREAKCOST);
			if (ProfileTankOptionButton.isActive())
				setBreakCount(A, S);
			ProfileTankOptionButton.draw(Batch, A.getBreakCount() > 0, A.breakUnlocked ? 2 : 4);
		}
	}
	
	private static void setBounces(Armament A, Squad S)
	{
		// if they can afford bounces, purchase it
		if (!A.bouncesUnlocked && S.getArmy().getReq() >= AppConfigs.Arms.BOUNCECOST) {
			spendReq(S, AppConfigs.Arms.BOUNCECOST);
			A.bouncesUnlocked = true;
		}
		
		if (A.getBounces() == 0 && A.bouncesUnlocked) {
			A.setBounces(3);
		} else {
			A.setBounces(0);
		}
	}
	
	private static void setDivCount(Armament A, Squad S)
	{
		if (!A.divUnlocked && S.getArmy().getReq() >= AppConfigs.Arms.DIVCOST) {
			spendReq(S, AppConfigs.Arms.DIVCOST);
			A.divUnlocked = true;
		}
		
		if (A.getDivCount() == 0 && A.divUnlocked) {
			A.setDivCount(8);
			A.setBreakCount(0);
			A.setIncinerate(false);
		} else {
			A.setDivCount(0);
		}
	}
	
	private static void setBreakCount(Armament A, Squad S)
	{
		if (!A.breakUnlocked && S.getArmy().getReq() >= AppConfigs.Arms.BREAKCOST) {
			spendReq(S, AppConfigs.Arms.BREAKCOST);
			A.breakUnlocked= true;
		}
		
		if (A.getBreakCount() == 0 && A.breakUnlocked) {
			A.setBreakCount(8);
			A.setDivCount(0);
			A.setIncinerate(false);
		} else {
			A.setBreakCount(0);
		}
	}
	
	private static void setIncinerate(Armament A, Squad S)
	{
		if (!A.incinerateUnlocked && S.getArmy().getReq() >= AppConfigs.Arms.INCINERATECOST) {
			spendReq(S, AppConfigs.Arms.INCINERATECOST);
			A.incinerateUnlocked = true;
		}
		
		if (A.doIncinerate() == false && A.incinerateUnlocked) {
			A.setIncinerate(true);
			A.setDivCount(0);
			A.setBreakCount(0);
		} else {
			A.setIncinerate(false);
		}
	}
	
	public static void spendReq(Squad S, int Req)
	{
		S.getArmy().spendRequisition(Req, new Vector2(S.getBBox().x + S.getBBox().x/2, S.getBBox().y + S.getBBox().height));
	}
}
