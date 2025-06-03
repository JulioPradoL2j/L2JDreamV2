package com.dream.game.model.pvpsystem.holders;

public class BonusRange
{
	private final int min;
	private final int max;
	private final double multiplier;
	
	public BonusRange(int min, int max, double multiplier)
	{
		this.min = min;
		this.max = max;
		this.multiplier = multiplier;
	}
	
	public boolean inRange(int value)
	{
		return value >= min && value <= max;
	}
	
	public double getMultiplier()
	{
		return multiplier;
	}
}