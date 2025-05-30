package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.QuestState;

public class RequestTutorialPassCmdToServer extends L2GameClientPacket
{
	private String _bypass = null;

	@Override
	protected void readImpl()
	{
		_bypass = readS();
	}

	@Override
	protected void runImpl()
	{
		System.out.println(_bypass);
		L2PcInstance player = getClient().getActiveChar();

		if (player == null)
			return;

		QuestState qs = player.getQuestState("255_Tutorial");
		if (qs != null)
		{
			qs.getQuest().notifyEvent(_bypass, null, player);
		}
	}

}