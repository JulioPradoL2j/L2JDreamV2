package com.dream.game.towerwars.holder;

import com.dream.data.xml.StatSet;

public class TowerWarsCrystals
{
	private final String _name;
	private final TowerWarsTower _inhibitor;
	private final TowerWarsTower _crystal;
	
	
	public TowerWarsCrystals(StatSet set, TowerWarsTower inhibitor, TowerWarsTower crystal)
	{
		_name = set.getString("name");
		_inhibitor = inhibitor;
		_crystal = crystal;
		
	}
	
	public String getName() { return _name; }
	public TowerWarsTower getInhibitor() { return _inhibitor; }
	public TowerWarsTower getCrystal() { return _crystal; }
}