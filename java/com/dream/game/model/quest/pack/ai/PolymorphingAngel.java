package com.dream.game.model.quest.pack.ai;

import java.util.HashMap;
import java.util.Map;

import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;

public class PolymorphingAngel extends L2AttackableAIScript
{
	private static final Map<Integer, Integer> ANGELSPAWNS = new HashMap<>();

	static
	{
		ANGELSPAWNS.put(20830, 20859);
		ANGELSPAWNS.put(21067, 21068);
		ANGELSPAWNS.put(21062, 21063);
		ANGELSPAWNS.put(20831, 20860);
		ANGELSPAWNS.put(21070, 21071);
	}

	public PolymorphingAngel()
	{
		super(-1, "polymorphing_angel", "ai");
		int[] temp =
		{
			20830,
			21067,
			21062,
			20831,
			21070
		};
		registerMobs(temp);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (ANGELSPAWNS.containsKey(npcId))
		{
			L2Attackable newNpc = (L2Attackable) addSpawn(ANGELSPAWNS.get(npcId), npc);
			newNpc.setRunning();
		}
		return super.onKill(npc, killer, isPet);
	}
}