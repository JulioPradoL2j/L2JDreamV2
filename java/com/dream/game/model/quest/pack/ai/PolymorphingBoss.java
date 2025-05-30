package com.dream.game.model.quest.pack.ai;

import java.util.HashMap;
import java.util.Map;

import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.NpcSay;

public class PolymorphingBoss extends L2AttackableAIScript
{
	private static final Map<Integer, Integer> BOSSPAWNS = new HashMap<>();

	static
	{
		BOSSPAWNS.put(45646, 45647);
	}

	public PolymorphingBoss()
	{
		super(-1, "polymorphing_boss", "ai");
		int[] temp =
		{
			45646
		};
		registerMobs(temp);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (BOSSPAWNS.containsKey(npcId))
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "Noooo i Can't die, i will return for kill all of yours!!!!"));
			waitSecs(30);
			L2Attackable newNpc = (L2Attackable) addSpawn(BOSSPAWNS.get(npcId), npc);
			waitSecs(3);
			newNpc.broadcastPacket(new NpcSay(newNpc.getObjectId(), 0, newNpc.getNpcId(), "I am back from Hell, prepare for Batlle!"));
			newNpc.setRunning();
		}
		return super.onKill(npc, killer, isPet);
	}

	public static void waitSecs(int i)
	{
		try
		{
			Thread.sleep(i * 1000);
		}
		catch (InterruptedException ie)
		{
			ie.printStackTrace();
		}
	}
}