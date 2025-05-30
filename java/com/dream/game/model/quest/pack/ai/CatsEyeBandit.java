package com.dream.game.model.quest.pack.ai;

import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.network.serverpackets.NpcSay;
import com.dream.tools.random.Rnd;

public class CatsEyeBandit extends Quest
{
	private final int CatsEyeBandit = 27038;
	private boolean FirstAttacked = false;

	public CatsEyeBandit()
	{
		super(-1, "cats_eye_bandit", "ai");
		addKillId(CatsEyeBandit);
		addAttackId(CatsEyeBandit);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		int objId = npc.getObjectId();
		if (FirstAttacked)
		{
			if (Rnd.get(100) < 40)
				return super.onAttack(npc, attacker, damage, isPet);
			npc.broadcastPacket(new NpcSay(objId, 0, npc.getNpcId(), "You are ridiculous, seriously thinking that I can kill me′?"));
		}
		else
		{
			FirstAttacked = true;
		}

		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == CatsEyeBandit)
		{
			int objId = npc.getObjectId();
			if (Rnd.get(100) < 80)
			{
				npc.broadcastPacket(new NpcSay(objId, 0, npc.getNpcId(), "Oh, this is sad, nothing I could do...."));
			}
			FirstAttacked = false;
		}
		else if (FirstAttacked)
		{
			addSpawn(npcId, npc);
		}
		return super.onKill(npc, killer, isPet);
	}
}