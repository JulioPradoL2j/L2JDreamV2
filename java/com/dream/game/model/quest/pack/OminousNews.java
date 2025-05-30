package com.dream.game.model.quest.pack;

import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.quest.QuestState;
import com.dream.game.model.quest.State;

public class OminousNews extends Quest
{
	private static String qn = "122_OminousNews";

	private final int MOIRA = 31979;
	private final int KARUDA = 32017;
	private final String defaulttxt = "<html><body>You do not have the quest NPC or simply do not meet the minimum requirements!</body></html>";

	public OminousNews()
	{
		super(122, qn, "Troubling news");
		addStartNpc(MOIRA);
		addTalkId(MOIRA);
		addTalkId(KARUDA);
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = defaulttxt;
		int id = st.getState();
		int cond = st.getInt("cond");
		if (id != State.COMPLETED)
		{
			htmltext = event;
			if (event.equalsIgnoreCase("31979-03.htm") && cond == 0)
			{
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
			}
			else if (event.equalsIgnoreCase("32017-02.htm"))
				if (cond == 1 && st.getInt("ok") == 1)
				{
					st.rewardItems(57, 8923);
					st.addExpAndSp(45151, 2310);
					st.unset("cond");
					st.unset("ok");
					st.exitQuest(false);
					st.playSound("ItemSound.quest_finish");
				}
				else
				{
					htmltext = defaulttxt;
				}
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getNpcId();
		String htmltext = defaulttxt;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		int id = st.getState();
		int cond = st.getInt("cond");
		if (id == State.COMPLETED)
		{
			htmltext = "<html><body>This quest has already been completed.</body></html>";
		}
		else if (npcId == MOIRA)
		{
			if (cond == 0)
			{
				if (player.getLevel() >= 20)
				{
					htmltext = "31979-02.htm";
				}
				else
				{
					htmltext = "31979-01.htm";
					st.exitQuest(true);
				}
			}
			else
			{
				htmltext = "31979-03.htm";
			}
		}
		else if (npcId == KARUDA && cond == 1 && id == State.STARTED)
		{
			htmltext = "32017-01.htm";
			st.set("ok", "1");
		}
		return htmltext;
	}
}