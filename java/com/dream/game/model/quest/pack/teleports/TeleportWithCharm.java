package com.dream.game.model.quest.pack.teleports;

import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.quest.QuestState;

public class TeleportWithCharm extends Quest
{
	private static String qn = "1100_teleport_with_charm";

	public static void main(String[] args)
	{
		new TeleportWithCharm();
	}

	private final int ORC_GATEKEEPER_CHARM = 1658;
	private final int DWARF_GATEKEEPER_TOKEN = 1659;
	private final int WHIRPY = 30540;

	private final int TAMIL = 30576;

	public TeleportWithCharm()
	{
		super(1100, qn, "Teleports");
		addStartNpc(WHIRPY);
		addStartNpc(TAMIL);
		addTalkId(WHIRPY);
		addTalkId(TAMIL);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = "";
		int npcId = npc.getNpcId();
		if (npcId == TAMIL)
		{
			if (st.getQuestItemsCount(ORC_GATEKEEPER_CHARM) >= 1)
			{
				st.takeItems(ORC_GATEKEEPER_CHARM, 1);
				player.teleToLocation(-80826, 149775, -3043);
				st.exitQuest(true);
			}
			else
			{
				st.exitQuest(true);
				htmltext = "<html><body>Portal Guardian Of Tamil:<br>";
				htmltext = htmltext + "You can not teleport without Talisman Custodian of the portal. I'll give it to you, if you follow my quest.";
				htmltext = htmltext + "</body></html>";
			}
		}
		else if (npcId == WHIRPY)
			if (st.getQuestItemsCount(DWARF_GATEKEEPER_TOKEN) >= 1)
			{
				st.takeItems(DWARF_GATEKEEPER_TOKEN, 1);
				player.teleToLocation(-80826, 149775, -3043);
				st.exitQuest(true);
			}
			else
			{
				st.exitQuest(true);
				htmltext = "<html><body>Guardian Of The Portal Virfi:<br>";
				htmltext = htmltext + "My sensors indicate to me that you have no Mark Keeper portal. If you actually obtain for me the star stones, I will give you the sign of the custodian of the portal.";
				htmltext = htmltext + "</body></html>";
			}
		return htmltext;
	}
}