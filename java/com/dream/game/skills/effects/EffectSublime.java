package com.dream.game.skills.effects;

import com.dream.game.model.L2Effect;
import com.dream.game.skills.Env;
import com.dream.game.templates.skills.L2EffectType;

public final class EffectSublime extends L2Effect
{
	public EffectSublime(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}

	@Override
	public boolean onActionTime()
	{
		if (getEffected() != getEffector())
		{
			getEffected().setIsInvul(true);
		}

		return false;
	}

	@Override
	public void onExit()
	{
		getEffected().setIsInvul(false);
	}

	@Override
	public boolean onStart()
	{
		getEffector().reduceCurrentHp(getEffector().getMaxHp() + 1, getEffector());
		if (getEffected() != getEffector())
		{
			getEffected().setIsInvul(true);
		}

		return true;
	}
}