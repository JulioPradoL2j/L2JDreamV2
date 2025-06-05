package com.dream.game.model.holders;

import com.dream.data.xml.StatSet;

import java.util.List;

public class PrizeHolder
{
	private final int _position;
	private final List<IntIntHolder> _rewards;
	
	public PrizeHolder(StatSet set)
	{
		_position = set.getInteger("position");
		_rewards = set.getIntIntHolderList("reward");
	}
	
	public int getPosition()
	{
		return _position;
	}
	
	public List<IntIntHolder> getRewards()
	{
		return _rewards;
	}
}
