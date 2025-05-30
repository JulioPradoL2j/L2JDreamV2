package com.dream.game.skills.effects;

import com.dream.game.model.L2Boss;
import com.dream.game.model.L2Effect;
import com.dream.game.skills.Env;
import com.dream.game.templates.skills.L2EffectType;

public final class EffectBlessingOfEva extends L2Effect
{

	public EffectBlessingOfEva(Env env, EffectTemplate template)
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
		return false;
	}

	@Override
	public void onExit()
	{
	}

	@Override
	public boolean onStart()
	{
		if (!(getEffected() instanceof L2Boss))
		{
			getEffected().getStatus().setCurrentHp(getEffected().getMaxHp());
			getEffected().getStatus().setCurrentCp(getEffected().getMaxCp());
			getEffected().getStatus().setCurrentMp(getEffected().getMaxMp());
		}
		else
		{
			getEffector().getStatus().setCurrentHp(getEffector().getMaxHp());
			getEffector().getStatus().setCurrentCp(getEffector().getMaxCp());
			getEffector().getStatus().setCurrentMp(getEffector().getMaxMp());
		}
		return true;
	}
}
