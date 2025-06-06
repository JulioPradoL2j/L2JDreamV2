package com.dream.game.model.holders;

import com.dream.data.xml.StatSet;

public class DressMeEffectHolder
{
	private final int _skillId;
	private final int _level;
	private final boolean _recurring;
	private final int _interval;
	
	public DressMeEffectHolder(StatSet set)
	{
		_skillId = set.getInteger("skillId", 0);
		_level = set.getInteger("level", 1);
		_recurring = set.getBool("recurring", false);
		_interval = set.getInteger("interval", 60);
	}
	
	public int getSkillId()
	{
		return _skillId;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public boolean isRecurring()
	{
		return _recurring;
	}
	
	public int getInterval()
	{
		return _interval;
	}
}