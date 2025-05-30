package com.dream.game.model.quest.pack;

import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.quest.QuestState;
import com.dream.game.model.quest.State;

public class WhatWomenWant extends Quest
{
	private static String qn = "2_WhatWomenWant";

	private final int ARUJIEN = 30223;
	private final int MIRABEL = 30146;
	private final int HERBIEL = 30150;
	private final int GREENIS = 30157;

	private final int ARUJIENS_LETTER1 = 1092;
	private final int ARUJIENS_LETTER2 = 1093;
	private final int ARUJIENS_LETTER3 = 1094;
	private final int POETRY_BOOK = 689;
	private final int GREENIS_LETTER = 693;

	private final int[] questItems =
	{
		GREENIS_LETTER,
		ARUJIENS_LETTER3,
		ARUJIENS_LETTER1,
		ARUJIENS_LETTER2,
		POETRY_BOOK
	};

	public WhatWomenWant()
	{
		super(2, qn, "What women want");
		addStartNpc(ARUJIEN);
		addTalkId(ARUJIEN);
		addTalkId(MIRABEL);
		addTalkId(HERBIEL);
		addTalkId(GREENIS);
		addTalkId(ARUJIEN);
		questItemIds = questItems;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;
		if (event.equalsIgnoreCase("aruen-3.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			if (st.getQuestItemsCount(ARUJIENS_LETTER1) == 0)
			{
				st.giveItems(ARUJIENS_LETTER1, 1);
			}
		}
		if (event.equalsIgnoreCase("aruen-6.htm"))
		{
			st.rewardItems(57, 2300);
			st.addExpAndSp(4254, 335);
			st.set("cond", "0");
			st.exitQuest(false);
			st.playSound("ItemSound.quest_finish");
		}
		if (event.equalsIgnoreCase("aruen-7.htm"))
		{
			st.takeItems(ARUJIENS_LETTER3, -1);
			st.giveItems(POETRY_BOOK, 1);
			st.set("cond", "4");
			st.playSound("ItemSound.quest_middle");
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "<html><body>You do not get the quest for this NPC or simply do not meet its minimum requirements!</body></html>";
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		int id = st.getState();
		if (id == State.COMPLETED)
			return "<html><body>This quest has already been completed.</body></html>";
		int npcId = npc.getNpcId();
		int cond = st.getInt("cond");
		if (id == State.CREATED && npcId == ARUJIEN)
			if (player.getLevel() >= 2)
			{
				htmltext = "aruen-1.htm";
			}
			else
			{
				htmltext = "nocondition.htm";
				st.exitQuest(true);
			}
		if (id == State.STARTED)
		{
			if (npcId == ARUJIEN)
			{
				if (cond > 0)
				{
					htmltext = "aruen-4.htm";
				}
				if (cond > 3)
				{
					htmltext = "aruen-8.htm";
				}

				if (cond == 3)
				{
					htmltext = "aruen-5.htm";
				}
				else if (cond == 5)
				{
					htmltext = "aruen-9.htm";
					st.takeItems(GREENIS_LETTER, -1);
					st.rewardItems(57, 1850);
					st.giveItems(113, 1);
					st.addExpAndSp(4254, 335);
					st.set("cond", "0");
					st.exitQuest(false);
					st.playSound("ItemSound.quest_finish");
				}
			}
			if (npcId == MIRABEL)
				if (cond == 1)
				{
					htmltext = "mirabel-1.htm";
					st.takeItems(ARUJIENS_LETTER1, -1);
					st.giveItems(ARUJIENS_LETTER2, 1);
					st.set("cond", "2");
					st.playSound("ItemSound.quest_middle");
				}
				else if (cond > 1)
				{
					htmltext = "mirabel-2.htm";
				}
			if (npcId == HERBIEL)
				if (cond == 2)
				{
					htmltext = "gerbiel-1.htm";
					st.takeItems(ARUJIENS_LETTER2, -1);
					st.giveItems(ARUJIENS_LETTER3, 1);
					st.set("cond", "3");
					st.playSound("ItemSound.quest_middle");
				}
				else if (cond > 2)
				{
					htmltext = "gerbiel-2.htm";
				}
			if (npcId == GREENIS)
				if (cond == 4)
				{
					htmltext = "grinis-1.htm";
					st.takeItems(POETRY_BOOK, -1);
					st.giveItems(GREENIS_LETTER, 1);
					st.set("cond", "5");
					st.playSound("ItemSound.quest_middle");
				}
				else if (cond > 4)
				{
					htmltext = "grinis-2.htm";
				}
		}
		return htmltext;
	}
}