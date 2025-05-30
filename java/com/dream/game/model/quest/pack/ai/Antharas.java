package com.dream.game.model.quest.pack.ai;

import com.dream.game.manager.grandbosses.AntharasManager;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.quest.QuestState;

public class Antharas extends Quest
{
	public static String QUEST = "antharas";
	private final int PORTAL_STONE = 3865;
	private final int HEART = 13001;
	private final int ANTHARAS_WEAK = 29066;
	private final int ANTHARAS_NORMAL = 29067;
	private final int ANTHARAS_STRONG = 29068;
	private final int BEHEMOTH = 29069;
	private QuestState st;

	public Antharas()
	{
		super(-1, QUEST, "ai");
		addStartNpc(HEART);
		addTalkId(HEART);
		addKillId(ANTHARAS_WEAK);
		addKillId(ANTHARAS_NORMAL);
		addKillId(ANTHARAS_STRONG);
		addKillId(BEHEMOTH);
		for (int i = 29070; i < 29077; i++)
		{
			addKillId(i);
		}
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		AntharasManager.getInstance().onKill(npc);
		if (npc.getNpcId() != ANTHARAS_NORMAL && npc.getNpcId() != ANTHARAS_STRONG && npc.getNpcId() != ANTHARAS_WEAK)
			return null;
		for (L2PcInstance pc : AntharasManager.getInstance().getPlayersInside())
		{
			st = pc.getQuestState(QUEST);
			if (st != null)
				if (st.getQuestItemsCount(8568) < 1)
				{
					st.giveItems(8568, 1);
				}
			st.exitQuest(true);
		}
		AntharasManager.getInstance().setCubeSpawn();
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(QUEST);
		String htmltext = "<html><body>Heart of Warding:<br>Your can't enter to lair!<br>You can't enter to Antharas lair.</body></html>";
		if (st == null)
		{
			st = newQuestState(player);
		}

		int npcId = npc.getNpcId();
		if (npcId == HEART)
		{
			if (player.isFlying())
				return "<html><body>Heart of Warding:<br>You cannot enter the lair in flight.</body></html>";

			if (AntharasManager.getInstance().isEnableEnterToLair())
			{
				if (st.getQuestItemsCount(PORTAL_STONE) >= 1)
				{
					st.takeItems(PORTAL_STONE, 1);
					AntharasManager.getInstance().setAntharasSpawnTask();
					player.teleToLocation(173826, 115333, -7708);
					return null;
				}
				st.exitQuest(true);
				return "<html><body>Heart of Warding:<br>You do not have the proper stones needed for teleport.<br>It is for the teleport where does 1 stone to you need.</body></html>";
			}
			st.exitQuest(true);
			return htmltext;
		}
		return htmltext;
	}
}
