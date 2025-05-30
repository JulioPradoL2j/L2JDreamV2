package com.dream.game.model.quest.pack.teleports;

import com.dream.Config;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.quest.QuestState;

public class CrumaTower extends Quest
{
	private static String qn = "1108_CrumaTower";

	public static void main(String[] args)
	{
		new CrumaTower();
	}

	private final int MOZELLA = 30483;

	public CrumaTower()
	{
		super(1108, qn, "Teleports");
		addStartNpc(MOZELLA);
		addTalkId(MOZELLA);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(qn);
		int npcId = npc.getNpcId();
		if (npcId == MOZELLA)
			if (player.getLevel() >= Config.CRUMA_TOWER_LEVEL_RESTRICT)
		{
			htmltext = htmltext + "<html><body>Portal Keeper Mozella:<br>";
			htmltext = htmltext + "ABOUT! You are too strong to pass in this portal. You apply a magnetic shield of the tower.<br>";
			htmltext = htmltext + "(Characters whose level has reached " + Config.CRUMA_TOWER_LEVEL_RESTRICT + " or more cannot enter the Tower of Cruma.)</body></html>";
		}
		else
		{
			player.teleToLocation(17724, 114004, -11672);
		}
		st.exitQuest(true);
		return htmltext;
	}
}