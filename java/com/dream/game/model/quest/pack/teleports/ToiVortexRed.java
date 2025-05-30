package com.dream.game.model.quest.pack.teleports;

import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.quest.QuestState;

import java.util.ArrayList;
import java.util.List;

public class ToiVortexRed extends Quest
{
	private static String qn = "1102_toivortex_red";

	public static void main(String[] args)
	{
		new ToiVortexRed();
	}

	private final int RED_DIMENSION_STONE = 4403;
	private final int[] VORTEXS =
	{
		30952,
		30953
	};

	private final List<Integer> VORTEXS_ID = new ArrayList<>();

	public ToiVortexRed()
	{
		super(1102, qn, "Teleports");
		for (int id : VORTEXS)
		{
			addStartNpc(id);
			addTalkId(id);
			VORTEXS_ID.add(id);
		}
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(qn);
		int npcId = npc.getNpcId();
		if (VORTEXS_ID.contains(npcId))
			if (st.getQuestItemsCount(RED_DIMENSION_STONE) >= 1)
			{
				st.takeItems(RED_DIMENSION_STONE, 1);
				player.teleToLocation(118558, 16659, 5987);
				st.exitQuest(true);
			}
			else
			{
				st.exitQuest(true);
				htmltext = "<html><head><body>Spacial Vortex:<br>You don't have a Stone of space required for teleportation.</body></html>";
			}
		return htmltext;
	}
}