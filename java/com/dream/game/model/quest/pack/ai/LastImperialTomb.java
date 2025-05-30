package com.dream.game.model.quest.pack.ai;

import com.dream.Config;
import com.dream.game.manager.lastimperialtomb.LastImperialTombManager;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.quest.QuestState;

public class LastImperialTomb extends Quest
{
	private final int GUIDE = 32011;

	public LastImperialTomb()
	{
		super(-1, "lastimperialtomb", "ai");
		addStartNpc(GUIDE);
		addTalkId(GUIDE);
		addKillId(18328);
		addKillId(18339);
		addKillId(18334);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == 18328)
		{
			LastImperialTombManager.getInstance().onKillHallAlarmDevice();
		}
		else if (npcId == 18339)
		{
			LastImperialTombManager.getInstance().onKillDarkChoirPlayer();
		}
		else if (npcId == 18334)
		{
			LastImperialTombManager.getInstance().onKillDarkChoirCaptain();
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState("lastimperialtomb");
		if (st == null)
			return null;

		int npcId = npc.getNpcId();
		if (npcId == GUIDE)
		{
			if (player.isFlying())
				return "<html><body>Imperial Tomb Guide:<br>You can not login during the flight.</body></html>";

			if (Config.LIT_REGISTRATION_MODE == 0)
			{
				if (LastImperialTombManager.getInstance().tryRegistrationCc(player))
				{
					LastImperialTombManager.getInstance().registration(player, npc);
				}
			}
			else if (Config.LIT_REGISTRATION_MODE == 1)
			{
				if (LastImperialTombManager.getInstance().tryRegistrationPt(player))
				{
					LastImperialTombManager.getInstance().registration(player, npc);
				}
			}
			else if (Config.LIT_REGISTRATION_MODE == 2)
				if (LastImperialTombManager.getInstance().tryRegistrationPc(player))
				{
					LastImperialTombManager.getInstance().registration(player, npc);
				}
		}
		return "";
	}
}