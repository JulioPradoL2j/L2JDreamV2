package com.dream.game.model.quest.pack.ai;

import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.network.serverpackets.NpcSay;
import com.dream.tools.random.Rnd;

public class DeluLizardmanSpecialCommander extends Quest
{
	private final int DeluLizardmanSpecialCommander = 21107;
	private boolean FirstAttacked = false;

	public DeluLizardmanSpecialCommander()
	{
		super(-1, "delu_lizardman_special_commander", "ai");
		addKillId(DeluLizardmanSpecialCommander);
		addAttackId(DeluLizardmanSpecialCommander);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		int objId = npc.getObjectId();
		if (FirstAttacked)
		{
			if (Rnd.get(100) < 40)
				return null;
			npc.broadcastPacket(new NpcSay(objId, 0, npc.getNpcId(), "Come on, I'll take you on!"));
		}
		else
		{
			FirstAttacked = true;
			npc.broadcastPacket(new NpcSay(objId, 0, npc.getNpcId(), "How dare you interrupt a sacred duel! You must be taught a lesson!"));
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == DeluLizardmanSpecialCommander)
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