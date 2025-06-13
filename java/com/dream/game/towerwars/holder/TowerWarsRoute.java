package com.dream.game.towerwars.holder;

import com.dream.data.xml.StatSet;

import java.util.List;

public class TowerWarsRoute
{
	private final String _name;
	private final List<TowerWarsTower> _towers;
	public TowerWarsRoute(StatSet set, List<TowerWarsTower> towers)
	{
		_name = set.getString("name");
		_towers = towers;
	}
	
	public String getName() { return _name; }
	public List<TowerWarsTower> getTowers() { return _towers; }
}