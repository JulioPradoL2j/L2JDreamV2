package com.dream.game.model.quest.pack;

import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.quest.QuestState;
import com.dream.game.model.quest.State;

public class ReleaseDarkElfElder extends Quest
{
	private static String qn = "3_ReleaseDarkelfElder1";

	private final int TALLOTH = 30141;

	private final int ONYX_BEAST_EYE = 1081;
	private final int TAINT_STONE = 1082;
	private final int SUCCUBUS_BLOOD = 1083;

	private final int OMEN_BEAST = 20031;
	private final int TAINTED_ZOMBIE = 20041;
	private final int STINK_ZOMBIE = 20046;
	private final int LESSER_SUCCUBUS = 20048;
	private final int LESSER_SUCCUBUS_TUREN = 20052;
	private final int LESSER_SUCCUBUS_TILFO = 20057;

	public ReleaseDarkElfElder()
	{
		super(3, qn, "How to remove the curse");
		addStartNpc(TALLOTH);
		addTalkId(TALLOTH);
		addKillId(OMEN_BEAST);
		addKillId(TAINTED_ZOMBIE);
		addKillId(STINK_ZOMBIE);
		addKillId(LESSER_SUCCUBUS);
		addKillId(LESSER_SUCCUBUS_TUREN);
		addKillId(LESSER_SUCCUBUS_TILFO);
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;
		if (event.equalsIgnoreCase("30141-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
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

		int npcId = npc.getNpcId();
		if (st.getInt("cond") == 1)
		{
			if (npcId == OMEN_BEAST && st.getQuestItemsCount(ONYX_BEAST_EYE) == 0)
			{
				st.giveItems(ONYX_BEAST_EYE, 1);
			}
			else if (contains(new int[]
			{
				TAINTED_ZOMBIE,
				STINK_ZOMBIE
			}, npcId) && st.getQuestItemsCount(TAINT_STONE) == 0)
			{
				st.giveItems(TAINT_STONE, 1);
			}
			else if (contains(new int[]
			{
				LESSER_SUCCUBUS,
				LESSER_SUCCUBUS_TUREN,
				LESSER_SUCCUBUS_TILFO
			}, npcId) && st.getQuestItemsCount(SUCCUBUS_BLOOD) == 0)
			{
				st.giveItems(SUCCUBUS_BLOOD, 1);
			}
			if (st.getQuestItemsCount(ONYX_BEAST_EYE) > 0 && st.getQuestItemsCount(TAINT_STONE) > 0 && st.getQuestItemsCount(SUCCUBUS_BLOOD) > 0)
			{
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "<html><body>You do not have the quest NPC or simply do not meet the minimum requirements!</body></html>";
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		int id = st.getState();
		int cond = st.getInt("cond");
		if (id == State.COMPLETED)
			return "<html><body>This quest has already been completed.</body></html>";
		else if (cond == 0)
		{
			if (player.getRace().ordinal() != 2)
			{
				htmltext = "30141-00.htm";
				st.exitQuest(true);
			}
			else if (player.getLevel() >= 16)
			{
				htmltext = "30141-02.htm";
			}
			else
			{
				htmltext = "30141-01.htm";
				st.exitQuest(true);
			}
		}
		else if (cond == 1)
		{
			htmltext = "30141-04.htm";
		}
		else if (cond == 2)
		{
			htmltext = "30141-06.htm";
			st.takeItems(ONYX_BEAST_EYE, -1);
			st.takeItems(TAINT_STONE, -1);
			st.takeItems(SUCCUBUS_BLOOD, -1);
			st.giveItems(956, 1);
			st.unset("cond");
			st.exitQuest(false);
			st.playSound("ItemSound.quest_finish");
		}
		return htmltext;
	}
}