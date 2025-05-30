package com.dream.game.model;

public final class L2SkillLearn
{
	private final int _id;
	private final int _level;
	private final String _name;
	private final int _spCost;
	private final int _minLevel;
	private final int _costid;
	private final int _costcount;

	public L2SkillLearn(int id, int lvl, int minLvl, String name, int cost, int costid, int costcount)
	{
		_id = id;
		_level = lvl;
		_minLevel = minLvl;
		_name = name.intern();
		_spCost = cost;
		_costid = costid;
		_costcount = costcount;
	}

	public int getCostCount()
	{
		return _costcount;
	}

	public int getId()
	{
		return _id;
	}

	public int getIdCost()
	{
		return _costid;
	}

	public int getLevel()
	{
		return _level;
	}

	public int getMinLevel()
	{
		return _minLevel;
	}

	public String getName()
	{
		return _name;
	}

	public int getSpCost()
	{
		return _spCost;
	}
}