package com.dream.game.model.pvpsystem.holders;

import com.dream.data.xml.StatSet;

import java.util.List;

public class PvPRank
{
	private final String _name;
	private final List<PvPTier> _tiers;
	
	public PvPRank(StatSet set, List<PvPTier> tiers)
	{
		_name = set.getString("name");
		_tiers = tiers;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public List<PvPTier> getTiers()
	{
		return _tiers;
	}
}
