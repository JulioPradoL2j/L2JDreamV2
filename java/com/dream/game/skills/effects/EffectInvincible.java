package com.dream.game.skills.effects;

import com.dream.game.model.L2Effect;
import com.dream.game.skills.Env;
import com.dream.game.templates.skills.L2EffectType;

public final class EffectInvincible extends L2Effect
{
	public EffectInvincible(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.INVINCIBLE;
	}

	@Override
	public boolean onActionTime()
	{
		getEffected().setIsInvul(true);
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
		getEffected().setIsInvul(true);
		return true;
	}
}
