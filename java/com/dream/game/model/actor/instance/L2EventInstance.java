package com.dream.game.model.actor.instance;

import com.dream.game.templates.chars.L2NpcTemplate;

public class L2EventInstance extends L2MonsterInstance
{
	private final L2PcInstance _owner;
	private int _level;

	public L2EventInstance(int objectId, L2NpcTemplate template, L2PcInstance owner)
	{
		super(objectId, template);
		_owner = owner;
	}

	@Override
	public final int getLevel()
	{
		return _level;
	}

	public L2PcInstance getOwner()
	{
		return _owner;
	}

	public void setLevel(int par)
	{
		_level = par;
	}
}