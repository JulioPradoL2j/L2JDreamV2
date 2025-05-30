package com.dream.game.model.quest.pack.ai;

import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.network.serverpackets.NpcSay;
import com.dream.tools.random.Rnd;

public class TurekOrcFootman extends Quest
{
	private final int TurekOrcFootman = 20499;
	private boolean FirstAttacked = false;

	public TurekOrcFootman()
	{
		super(-1, "turek_orc_footman", "ai");
		addKillId(TurekOrcFootman);
		addAttackId(TurekOrcFootman);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		int objId = npc.getObjectId();
		if (FirstAttacked)
		{
			if (Rnd.get(100) < 40)
				return null;
			npc.broadcastPacket(new NpcSay(objId, 0, npc.getNpcId(), "There is no reason for you to kill me! I have nothing you need!"));
		}
		else
		{
			FirstAttacked = true;
			npc.broadcastPacket(new NpcSay(objId, 0, npc.getNpcId(), "We shall see about that!"));
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == TurekOrcFootman)
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