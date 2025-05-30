package com.dream.game.skills.effects;

import com.dream.game.model.L2Effect;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.skills.Env;
import com.dream.game.templates.skills.L2EffectType;

public final class EffectCombatPointHealOverTime extends L2Effect
{
	public EffectCombatPointHealOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.COMBAT_POINT_HEAL_OVER_TIME;
	}

	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
			return false;

		double cp = getEffected().getStatus().getCurrentCp();
		double maxcp = getEffected().getMaxCp();
		cp += calc();
		if (cp > maxcp)
		{
			cp = maxcp;
		}
		getEffected().getStatus().setCurrentCp(cp);
		StatusUpdate sump = new StatusUpdate(getEffected());
		sump.addAttribute(StatusUpdate.CUR_CP, (int) cp);
		getEffected().sendPacket(sump);
		return true;
	}
}