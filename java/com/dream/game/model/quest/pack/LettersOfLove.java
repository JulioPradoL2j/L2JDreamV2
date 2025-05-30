package com.dream.game.model.quest.pack;

import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.quest.QuestState;
import com.dream.game.model.quest.State;

public class LettersOfLove extends Quest
{
	private static String qn = "1_LettersOfLove";

	private final int DARIN = 30048;
	private final int ROXXY = 30006;
	private final int BAULRO = 30033;

	private final int DARINGS_LETTER = 687;
	private final int RAPUNZELS_KERCHIEF = 688;
	private final int DARINGS_RECEIPT = 1079;
	private final int BAULS_POTION = 1080;

	private final int NECKLACE = 906;

	private final int[] questItems =
	{
		DARINGS_LETTER,
		RAPUNZELS_KERCHIEF,
		DARINGS_RECEIPT,
		BAULS_POTION
	};

	public LettersOfLove()
	{
		super(1, qn, "Letters Love");
		addStartNpc(DARIN);
		addTalkId(DARIN);
		addTalkId(ROXXY);
		addTalkId(BAULRO);
		questItemIds = questItems;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;
		if (event.equalsIgnoreCase("darin-5.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			if (st.getQuestItemsCount(DARINGS_LETTER) == 0)
			{
				st.giveItems(DARINGS_LETTER, 1);
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "<html><body>You do not have the quest NPC or simply do not meet the minimum requirements!!</body></html>";
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		int id = st.getState();
		if (id == State.COMPLETED)
			return "<html><body>This quest has already been completed.</body></html>";
		int npcId = npc.getNpcId();
		int cond = st.getInt("cond");
		if (id == State.CREATED && npcId == DARIN)
			if (player.getLevel() >= 2)
			{
				htmltext = "darin-1.htm";
			}
			else
			{
				htmltext = "nocondition.htm";
				st.exitQuest(true);
			}
		if (id == State.STARTED)
			if (npcId == DARIN)
			{
				if (cond > 0)
				{
					htmltext = "darin-6.htm";
				}
				if (cond > 2)
				{
					htmltext = "darin-8.htm";
				}

				if (cond == 2)
				{
					htmltext = "darin-7.htm";
					st.takeItems(RAPUNZELS_KERCHIEF, -1);
					st.giveItems(DARINGS_RECEIPT, 1);
					st.set("cond", "3");
					st.playSound("ItemSound.quest_middle");
				}
				else if (cond == 4)
				{
					htmltext = "darin-9.htm";
					st.takeItems(BAULS_POTION, -1);
					st.rewardItems(57, 2466);
					st.giveItems(NECKLACE, 1);
					st.addExpAndSp(5672, 446);
					st.unset("cond");
					st.exitQuest(false);
					st.playSound("ItemSound.quest_finish");
				}
			}
			else if (npcId == ROXXY)
			{
				if (cond == 1)
				{
					htmltext = "roxxy-1.htm";
					st.takeItems(DARINGS_LETTER, -1);
					st.giveItems(RAPUNZELS_KERCHIEF, 1);
					st.set("cond", "2");
					st.playSound("ItemSound.quest_middle");
				}
				else if (cond == 2)
				{
					htmltext = "roxxy-2.htm";
				}
				else if (cond > 2)
				{
					htmltext = "roxxy-3.htm";
				}
			}
			else if (npcId == BAULRO)
				if (cond == 3)
				{
					htmltext = "baulro-1.htm";
					st.takeItems(DARINGS_RECEIPT, -1);
					st.giveItems(BAULS_POTION, 1);
					st.set("cond", "4");
					st.playSound("ItemSound.quest_middle");
				}
				else if (cond > 3)
				{
					htmltext = "baulro-2.htm";
				}
		return htmltext;
	}
}