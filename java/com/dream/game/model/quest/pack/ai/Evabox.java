package com.dream.game.model.quest.pack.ai;

import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.model.L2Effect;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.tools.random.Rnd;

public class Evabox extends Quest
{
	private static void dropItem(L2Npc npc, int itemId, int count)
	{
		L2ItemInstance ditem = ItemTable.createItem("quest", itemId, count, null);
		ditem.dropMe(npc, npc.getX(), npc.getY(), npc.getZ());
	}

	private final int BOX = 32342;
	private final int KISS_OF_EVA[] =
	{
		1073,
		3143,
		3252
	};

	private final int REWARDS[] =
	{
		9692,
		9693
	};

	public Evabox()
	{
		super(-1, "evabox", "ai");
		addKillId(BOX);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		boolean found = false;
		for (L2Effect effect : player.getAllEffects())
		{
			for (int kof : KISS_OF_EVA)
				if (effect.getSkill().getId() == kof)
				{
					found = true;
				}
		}
		if (found)
		{
			int dropid = Rnd.get(REWARDS.length);
			dropItem(npc, REWARDS[dropid], 1);
		}
		return null;
	}
}