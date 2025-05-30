package com.dream.game.skills.effects;

import com.dream.game.model.L2Effect;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.skills.Env;
import com.dream.game.templates.skills.L2EffectType;

public final class EffectHealLimit extends L2Effect
{
	public EffectHealLimit(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.DEBUFF;
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}

	@Override
	public void onExit()
	{
		getEffected().setHealLimit(0);
	}

	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof L2PcInstance)
		{
			getEffected().setHealLimit((int) calc());
		}
		return true;
	}
}
