package com.dream.game.model.actor.instance;

import com.dream.game.model.actor.L2Character;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2LarvaInstance extends L2MonsterInstance
{

	public L2LarvaInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean canReduceHp(double damage, L2Character attacker)
	{
		return getCurrentHp() - damage > 10;
	}
}