package com.dream.game.model.quest.pack;

import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.quest.QuestState;
import com.dream.game.model.quest.State;

public class BlackSwan extends Quest
{
	private static String qn = "351_BlackSwan";

	private final int ADENA = 57;
	private final int ORDER_OF_GOSTA = 4296;
	private final int LIZARD_FANG = 4297;
	private final int BARREL_OF_LEAGUE = 4298;
	private final int BILL_OF_IASON_HEINE = 4407;
	private final int CHANCE = 10;
	private final int CHANCE2 = 15;
	private final int CHANCE_barrel = 0;
	private final int[] questItems =
	{
		ORDER_OF_GOSTA,
		BARREL_OF_LEAGUE,
		LIZARD_FANG
	};

	public BlackSwan()
	{
		super(351, qn, "Black Swan");
		addStartNpc(30916);
		addTalkId(30916);
		addTalkId(30969);
		addTalkId(30897);
		addKillId(20784);
		addKillId(20785);
		addKillId(21639);
		addKillId(21640);
		addKillId(21642);
		addKillId(21643);
		questItemIds = questItems;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;
		int amount = st.getQuestItemsCount(LIZARD_FANG);
		int amount2 = st.getQuestItemsCount(BARREL_OF_LEAGUE);
		int bonus = 0;
		if (event.equalsIgnoreCase("30916-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.giveItems(ORDER_OF_GOSTA, 1);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30969-02a.htm") && amount > 0)
		{
			if (amount > 10)
			{
				bonus = 3880;
			}
			htmltext = "30969-02.htm";
			st.giveItems(ADENA, amount * 20 + bonus);
			st.takeItems(LIZARD_FANG, -1);
		}
		else if (event.equalsIgnoreCase("30969-03a.htm"))
		{
			if (amount2 > 0)
			{
				htmltext = "30969-03.htm";
				st.set("cond", "2");
				st.giveItems(ADENA, 3880);
				st.giveItems(BILL_OF_IASON_HEINE, amount2);
				st.takeItems(BARREL_OF_LEAGUE, -1);
			}
		}
		else if (event.equalsIgnoreCase("30969-06.htm"))
			if (amount + amount2 > 0)
			{
				st.exitQuest(true);
				st.playSound("ItemSound.quest_finish");
				htmltext = "30969-07.htm";
			}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;
		if (st.getState() != State.STARTED)
			return null;
		int random = QuestState.getRandom(20);
		if (random < CHANCE)
		{
			st.giveItems(LIZARD_FANG, 1);
			st.playSound("ItemSound.quest_itemget");
			if (random == CHANCE_barrel)
			{
				st.giveItems(BARREL_OF_LEAGUE, 1);
				st.set("cond", "2");
			}
		}
		else if (random < CHANCE2)
		{
			st.giveItems(LIZARD_FANG, 2);
			st.playSound("ItemSound.quest_itemget");
			if (random == CHANCE_barrel)
			{
				st.giveItems(BARREL_OF_LEAGUE, 1);
				st.set("cond", "2");
			}
		}
		else if (QuestState.getRandom(100) < 4)
		{
			st.giveItems(BARREL_OF_LEAGUE, 1);
			st.playSound("ItemSound.quest_itemget");
			st.set("cond", "2");
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = "<html><body>You do not have the quest NPC or simply do not meet the minimum requirements!</body></html>";
		if (st == null)
			return htmltext;
		int npcId = npc.getNpcId();
		int id = st.getState();
		if (npcId != 30916 && id != State.STARTED)
			return htmltext;

		int level = player.getLevel();
		int cond = st.getInt("cond");
		if (npcId == 30916)
		{
			if (id == State.CREATED)
			{
				if (level >= 32)
				{
					htmltext = "30916-01.htm";
				}
				else
				{
					htmltext = "30916-00.htm";
					st.exitQuest(true);
				}
			}
			else if (cond != 0)
			{
				htmltext = "30916-04.htm";
			}
		}
		else if (npcId == 30969 && cond > 0)
		{
			htmltext = "30969-01.htm";
		}
		else if (npcId == 30897)
			if (st.getQuestItemsCount(BILL_OF_IASON_HEINE) > 0)
			{
				htmltext = "30897-01.htm";
			}
			else
			{
				htmltext = "30897-02.htm";
			}
		return htmltext;
	}
}