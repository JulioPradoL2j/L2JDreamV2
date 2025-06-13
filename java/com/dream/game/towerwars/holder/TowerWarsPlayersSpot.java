package com.dream.game.towerwars.holder;

import com.dream.data.xml.StatSet;

public class TowerWarsPlayersSpot
{
	private final int _count, _x, _y, _z;
	
	public TowerWarsPlayersSpot(StatSet set)
	{
		_count = set.getInteger("count");
		_x = set.getInteger("x");
		_y = set.getInteger("y");
		_z = set.getInteger("z");
	}
	public int getCount() { return _count; }
	public int getX() { return _x; }
	public int getY() { return _y; }
	public int getZ() { return _z; }
}
