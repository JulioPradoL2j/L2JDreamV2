package com.dream.game.model.actor.instance;

import com.dream.game.manager.clanhallsiege.RainbowSpringSiege;
import com.dream.game.model.actor.L2Character;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2HotSpringSquashInstance extends L2MonsterInstance
{
	public L2HotSpringSquashInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean canReduceHp(double damage, L2Character attacker)
	{
		return false;
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;
		RainbowSpringSiege.getInstance().onDieSquash(this);
		return true;
	}
}