package com.dream.game.skills.effects;

import com.dream.game.model.L2Effect;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.skills.Env;
import com.dream.game.templates.skills.L2EffectType;

public class EffectSeed extends L2Effect
{

	private enum Seeds
	{
		SeedOfWind,
		SeedOfWater,
		SeedOfFire
	}

	public EffectSeed(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}

	public int getForce()
	{
		L2PcInstance player = getEffected().getActingPlayer();
		if (player == null)
			return 0;
		return player._seeds[Seeds.valueOf(getStackType()).ordinal()];
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}

	@Override
	protected void onExit()
	{
		L2PcInstance player = getEffected().getActingPlayer();
		if (player == null)
			return;
		if (_exitEffector == null || _exitEffector == getEffector())
		{
			player._seeds[Seeds.valueOf(getStackType()).ordinal()] = 0;
		}
	}

	@Override
	protected boolean onStart()
	{
		L2PcInstance player = getEffected().getActingPlayer();
		if (player == null)
			return false;
		player._seeds[Seeds.valueOf(getStackType()).ordinal()]++;
		return true;
	}
}