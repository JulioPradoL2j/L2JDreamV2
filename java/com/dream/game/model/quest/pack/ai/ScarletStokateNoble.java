package com.dream.game.model.quest.pack.ai;

import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.tools.random.Rnd;

public class ScarletStokateNoble extends Quest
{
	private final int ScarletStokateNoble = 21378;
	private final int ScarletStokateNobleB = 21652;

	public ScarletStokateNoble()
	{
		super(-1, "scarlet_stokate_noble", "ai");
		addKillId(ScarletStokateNoble);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == ScarletStokateNoble && Rnd.get(100) < 20)
		{
			for (int i = 0; i < 1 + Rnd.get(5); i++)
			{
				L2Attackable mob = (L2Attackable) addSpawn(ScarletStokateNobleB, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
				mob.addDamageHate(isPet ? killer.getPet() : killer, 0, 100);
			}
		}
		return null;
	}
}