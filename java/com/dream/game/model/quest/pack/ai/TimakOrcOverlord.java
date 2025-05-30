package com.dream.game.model.quest.pack.ai;

import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.network.serverpackets.NpcSay;
import com.dream.tools.random.Rnd;

public class TimakOrcOverlord extends Quest
{
	private final int TimakOrcOverlord = 20588;
	private boolean FirstAttacked = false;

	public TimakOrcOverlord()
	{
		super(-1, "timak_orc_overlord", "ai");
		addKillId(TimakOrcOverlord);
		addAttackId(TimakOrcOverlord);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		int objId = npc.getObjectId();
		if (FirstAttacked)
		{
			if (Rnd.get(100) < 40)
				return null;
			npc.broadcastPacket(new NpcSay(objId, 0, npc.getNpcId(), "Dear ultimate power!!!"));
		}
		else
		{
			FirstAttacked = true;
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == TimakOrcOverlord)
		{
			FirstAttacked = false;
		}
		else if (FirstAttacked)
		{
			addSpawn(npcId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
		}
		return null;
	}
}