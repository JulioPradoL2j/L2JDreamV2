package com.dream.game.skills.effects;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.model.L2Effect;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2NpcInstance;
import com.dream.game.model.actor.instance.L2SiegeSummonInstance;
import com.dream.game.network.serverpackets.StartRotation;
import com.dream.game.network.serverpackets.StopRotation;
import com.dream.game.skills.Env;
import com.dream.game.templates.skills.L2EffectType;

public final class EffectBlindingBlow extends L2Effect
{
	public EffectBlindingBlow(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BLUFF;
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
		if (getEffected() instanceof L2NpcInstance)
			return false;
		if (getEffected() instanceof L2Npc && ((L2Npc) getEffected()).getNpcId() == 35062)
			return false;
		if (getEffected() instanceof L2SiegeSummonInstance)
			return false;

		getEffected().getAI().setIntention(CtrlIntention.IDLE, getEffector());
		getEffected().broadcastPacket(new StartRotation(getEffected().getObjectId(), getEffected().getHeading(), 1, 65535));
		getEffected().broadcastPacket(new StopRotation(getEffected().getObjectId(), getEffector().getHeading(), 65535));
		getEffected().setHeading(getEffector().getHeading());
		return true;
	}
}
