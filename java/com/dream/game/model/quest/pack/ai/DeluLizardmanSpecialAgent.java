package com.dream.game.model.quest.pack.ai;

import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.network.serverpackets.NpcSay;
import com.dream.tools.random.Rnd;

public class DeluLizardmanSpecialAgent extends Quest
{
	private final int DeluLizardmanSpecialAgent = 21105;
	private boolean FirstAttacked = false;

	public DeluLizardmanSpecialAgent()
	{
		super(-1, "delu_lizardman_special_agent", "ai");
		addKillId(DeluLizardmanSpecialAgent);
		addAttackId(DeluLizardmanSpecialAgent);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		int objId = npc.getObjectId();
		if (FirstAttacked)
		{
			if (Rnd.get(100) < 15)
				return super.onAttack(npc, attacker, damage, isPet);
			npc.broadcastPacket(new NpcSay(objId, 0, npc.getNpcId(), "Hey, I challenge you to a duel right here!"));
		}
		else
		{
			FirstAttacked = true;
			npc.broadcastPacket(new NpcSay(objId, 0, npc.getNpcId(), "How did you dare to attack guys help!"));
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == DeluLizardmanSpecialAgent)
		{
			FirstAttacked = false;
		}
		else if (FirstAttacked)
		{
			addSpawn(npcId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
		}
		return super.onKill(npc, killer, isPet);
	}
}