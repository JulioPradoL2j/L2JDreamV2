package com.dream.game.model.pvpsystem.holders;

import java.util.ArrayList;
import java.util.List;

public class DecaySettings
{
	private final List<DecayRule> rules = new ArrayList<>();
	private int intervalHours = 24;
	
	public void addRule(DecayRule rule)
	{
		rules.add(rule);
	}
	
	public void setIntervalHours(int intervalHours)
	{
		this.intervalHours = intervalHours;
	}
	
	public int getIntervalHours()
	{
		return intervalHours;
	}
	
	public double getDecayPercentForPoints(int points)
	{
		for (DecayRule rule : rules)
		{
			if (rule.matches(points))
			{
				return rule.getDecayPercent();
			}
		}
		return 0.0;
	}
	
	public List<DecayRule> getRules()
	{
		return rules;
	}
}
