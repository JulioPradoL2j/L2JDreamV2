package com.dream.game.towerwars.holder;

import com.dream.data.xml.StatSet;

public class TowerWarsTower
{
	private final int _npcId;
	private final int _x, _y, _z;
	private final int _order;
	
	public TowerWarsTower(StatSet set)
	{
		_npcId = set.getInteger("npcId");
		_x = set.getInteger("x");
		_y = set.getInteger("y");
		_z = set.getInteger("z");
		_order = set.getInteger("order", 0);
	}
	
	public int getNpcId() { return _npcId; }
	public int getX() { return _x; }
	public int getY() { return _y; }
	public int getZ() { return _z; }
	public int getOrder() { return _order; }
}
