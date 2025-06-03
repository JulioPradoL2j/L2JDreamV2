package com.dream.game.model.pvpsystem.holders;

public class DecayRule
{
	private final int minPoints;
	private final int maxPoints;
	private final double decayPercent;
	
	public DecayRule(int minPoints, int maxPoints, double decayPercent)
	{
		this.minPoints = minPoints;
		this.maxPoints = maxPoints;
		this.decayPercent = decayPercent;
	}
	
	public boolean matches(int points)
	{
		return points >= minPoints && points <= maxPoints;
	}
	
	public double getDecayPercent()
	{
		return decayPercent;
	}
}
