package com.dream.game.model.quest.pack.teleports;

import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.quest.QuestState;

public class ElrokiTeleport extends Quest
{
	private static String qn = "6111_ElrokiTeleporters";

	public static void main(String[] args)
	{
		new ElrokiTeleport();
	}

	public ElrokiTeleport()
	{
		super(6111, qn, "Teleports");
		addStartNpc(32111);
		addStartNpc(32112);
		addTalkId(32111);
		addTalkId(32112);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(qn);
		int npcId = npc.getNpcId();
		if (npcId == 32111)
		{
			if (player.isInCombat())
			{
				htmltext = "<html><body>Orahochin:<br>You came to me with a dinosaur?<br1>";
				htmltext = htmltext + "And now you want me to sent you our abode? My responsibility to protect our new home. I'm not going to make our House a shelter for such adventurers!<br1>";
				htmltext = htmltext + "In my rules do not allow those who are in battle. Return as the end of their fight.</body></html>";
			}
			else
			{
				player.teleToLocation(4990, -1879, -3178);
			}
		}
		else if (npcId == 32112)
		{
			player.teleToLocation(7557, -5513, -3221);
		}
		st.exitQuest(true);
		return htmltext;
	}
}