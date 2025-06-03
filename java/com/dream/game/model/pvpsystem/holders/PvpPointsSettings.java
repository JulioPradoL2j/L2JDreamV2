package com.dream.game.model.pvpsystem.holders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PvpPointsSettings
{
	private final Map<String, Integer> basePoints = new HashMap<>();
	private final List<BonusRange> bonusRanges = new ArrayList<>();
	
	public void addBasePoint(String type, int points)
	{
		basePoints.put(type.toLowerCase(), points);
	}
	
	public void addBonusRange(BonusRange range)
	{
		bonusRanges.add(range);
	}
	
	public int calculatePoints(String type, int enemyPoints)
	{
		int base = basePoints.getOrDefault(type.toLowerCase(), 0);
		double multiplier = 1.0;
		
		for (BonusRange range : bonusRanges)
		{
			if (range.inRange(enemyPoints))
			{
				multiplier = range.getMultiplier();
				break;
			}
		}
		
		return (int) Math.round(base * multiplier);
	}
	
}
