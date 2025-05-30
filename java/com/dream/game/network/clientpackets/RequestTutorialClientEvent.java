package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.QuestState;

public class RequestTutorialClientEvent extends L2GameClientPacket
{
	private int _event;

	@Override
	protected void readImpl()
	{
		_event = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();

		if (player == null)
			return;

		QuestState qs = player.getQuestState("255_Tutorial");
		if (qs != null)
		{
			qs.getQuest().notifyEvent("CE" + _event + "", null, player);
		}
	}

}