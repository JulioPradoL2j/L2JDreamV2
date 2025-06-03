package com.dream.game.model.pvpsystem.holders;

import com.dream.data.xml.StatSet;
import com.dream.game.model.holders.IntIntHolder;

import java.util.List;

public class PvPTier
{
	private final int _level;
	private final int _pointsRequired;
	private final List<IntIntHolder> _rewards;
	
	public PvPTier(StatSet set)
	{
		_level = set.getInteger("level");
		_pointsRequired = set.getInteger("pointsRequired");
		_rewards = set.getIntIntHolderList("reward");
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public int getPointsRequired()
	{
		return _pointsRequired;
	}
	
	public List<IntIntHolder> getMaterials()
	{
		return _rewards;
	}
}
