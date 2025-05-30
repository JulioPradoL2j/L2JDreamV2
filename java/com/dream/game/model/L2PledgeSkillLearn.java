package com.dream.game.model;

public final class L2PledgeSkillLearn
{
	private final int _id;
	private final int _level;
	private final String _name;
	private final int _repCost;
	private final int _baseLvl;
	private final int _itemId;

	public L2PledgeSkillLearn(int id, int lvl, int baseLvl, String name, int cost, int itemId)
	{
		_id = id;
		_level = lvl;
		_baseLvl = baseLvl;
		_name = name.intern();
		_repCost = cost;
		_itemId = itemId;
	}

	public int getBaseLevel()
	{
		return _baseLvl;
	}

	public int getId()
	{
		return _id;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public int getLevel()
	{
		return _level;
	}

	public String getName()
	{
		return _name;
	}

	public int getRepCost()
	{
		return _repCost;
	}
}