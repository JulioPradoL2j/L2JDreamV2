package com.dream.game.model.quest.pack.ai;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;

public class FindAndAttackMaster extends L2AttackableAIScript
{
	public FindAndAttackMaster()
	{
		super(-1, "FindAndAttackMaster", "ai");
		int[] temp =
		{
			20965,
			20966,
			20967,
			20968,
			20969,
			20970,
			20971,
			20972,
			20973
		};
		registerMobs(temp);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if (player == null)
			return null;

		L2Character attacker = isPet ? player.getPet().getOwner() : player;
		npc.setIsRunning(true);
		((L2Attackable) npc).addDamageHate(attacker, 0, 999);
		npc.getAI().setIntention(CtrlIntention.ATTACK, attacker);

		return super.onAttack(npc, player, damage, isPet);
	}
}