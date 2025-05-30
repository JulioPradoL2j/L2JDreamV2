package com.dream.game.model.quest.pack.ai;

import java.util.Map;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.sql.SpawnTable;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.model.quest.Quest;
import com.dream.game.network.serverpackets.NpcSay;
import com.dream.game.network.serverpackets.PlaySound;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.game.network.serverpackets.SpecialCamera;

public class DrChaos extends Quest
{
	private static final int DOCTER_CHAOS = 32033;
	private static final int STRANGE_MACHINE = 32032;
	private static final int CHAOS_GOLEM = 25512;
	private static boolean _IsGolemSpawned;

	public DrChaos()
	{
		super(-1, "Doctor Chaos", "ai");
		addFirstTalkId(32033);
		_IsGolemSpawned = false;
	}

	public L2Npc FindTemplate(int npcId)
	{
		L2Npc npcInstance = null;
		L2Spawn spawn;
		Map<Integer, L2Spawn> values = SpawnTable.getInstance().getSpawnTable();
		for (int i = 0; i < values.size(); i++)
		{
			spawn = values.get(i);
			if (spawn != null && spawn.getNpcid() == npcId)
			{
				npcInstance = spawn.getLastSpawn();
				break;
			}
		}
		return npcInstance;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("Chat"))
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "Fools! Why are you still not elope? It's time you teach!"));
			startQuestTimer("1", 3000, npc, player);
		}
		else if (event.equalsIgnoreCase("1"))
		{
			L2Npc machine_instance = FindTemplate(STRANGE_MACHINE);
			if (machine_instance != null)
			{
				npc.getAI().setIntention(CtrlIntention.ATTACK, machine_instance);
				machine_instance.broadcastPacket(new SpecialCamera(machine_instance.getObjectId(), 1, -200, 15, 10000, 20000));
			}
			else
			{
				startQuestTimer("2", 2000, npc, player);
			}
			startQuestTimer("3", 10000, npc, player);
		}
		else if (event.equalsIgnoreCase("2"))
		{
			npc.broadcastPacket(new SocialAction(npc, 3));
		}
		else if (event.equalsIgnoreCase("3"))
		{
			npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1, -150, 10, 3000, 20000));
			startQuestTimer("4", 2500, npc, player);
		}
		else if (event.equalsIgnoreCase("4"))
		{
			npc.getAI().setIntention(CtrlIntention.MOVE_TO, new L2CharPosition(96055, -110759, -3312, 0));
			startQuestTimer("5", 2000, npc, player);
		}
		else if (event.equalsIgnoreCase("5"))
		{
			player.teleToLocation(94832, -112624, -3304);
			npc.teleToLocation(-113091, -243942, -15536);
			if (!_IsGolemSpawned)
			{
				L2Npc golem = addSpawn(CHAOS_GOLEM, 94640, -112496, -3336, 0, false, 0);
				_IsGolemSpawned = true;
				startQuestTimer("6", 1000, golem, player);
				player.sendPacket(new PlaySound(1, "Rm03_A", 0, 0, 0, 0, 0));
			}
		}
		else if (event.equalsIgnoreCase("6"))
		{
			npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 30, -200, 20, 6000, 8000));
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (npc.getNpcId() == DOCTER_CHAOS)
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "How do you sme vtorgats on my territory? You're not scared?"));
			npc.getAI().setIntention(CtrlIntention.MOVE_TO, new L2CharPosition(96323, -110914, -3328, 0));
			this.startQuestTimer("Chat", 3000, npc, player);
		}
		return null;
	}
}