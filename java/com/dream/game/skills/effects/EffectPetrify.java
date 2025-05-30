package com.dream.game.skills.effects;

import com.dream.game.model.L2Effect;
import com.dream.game.skills.AbnormalEffect;
import com.dream.game.skills.Env;
import com.dream.game.templates.skills.L2EffectType;

public final class EffectPetrify extends L2Effect
{
	public EffectPetrify(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.PETRIFY;
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}

	@Override
	public void onExit()

	{
		getEffected().stopAbnormalEffect(AbnormalEffect.HOLD_2);
		getEffected().stopParalyze(this);
		getEffected().setIsPetrified(false);
	}

	@Override
	public boolean onStart()
	{
		getEffected().startAbnormalEffect(AbnormalEffect.HOLD_2);
		getEffected().startParalyze();
		getEffected().setIsPetrified(true);
		return true;
	}
}
