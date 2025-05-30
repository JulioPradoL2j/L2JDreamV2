package com.dream.game.ai;

import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character.AIAccessor;
import com.dream.game.model.actor.instance.L2BaiumAngelInstance;
import com.dream.game.model.actor.instance.L2GrandBossInstance;
import com.dream.tools.random.Rnd;

public class L2BaiumAngelAI extends L2AttackableAI
{

	public L2BaiumAngelAI(AIAccessor accessor)
	{
		super(accessor);
	}

	@Override
	protected void thinkActive()
	{

		if (Rnd.get(100) < 30)
		{
			L2BaiumAngelInstance angel = (L2BaiumAngelInstance) _actor;
			L2GrandBossInstance boss = angel.getBoss();
			if (boss != null)
			{
				L2Skill sk = SkillTable.getInstance().getInfo(4201, 10);
				if (sk != null)
				{
					if (_actor.getDistanceSq(boss) > sk.getCastRange() * sk.getCastRange())
					{
						moveToPawn(boss, sk.getCastRange());
						return;
					}
					_actor.setTarget(boss);
					_actor.doCast(sk);
					return;
				}
			}

		}
		super.thinkActive();
	}

	@Override
	protected void thinkIdle()
	{
		thinkActive();
	}
}