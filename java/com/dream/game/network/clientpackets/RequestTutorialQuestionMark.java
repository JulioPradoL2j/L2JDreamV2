package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.QuestState;

public class RequestTutorialQuestionMark extends L2GameClientPacket
{
	private int _id;

	@Override
	protected void readImpl()
	{
		_id = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();

		if (player == null)
			return;

		if (_id < 0)
		{
			if (player._event != null)
			{
				player._event.onCommand(player, "Mark", String.valueOf(_id));
			}
		}
		else
		{
			QuestState qs = player.getQuestState("255_Tutorial");
			if (qs != null)
			{
				qs.getQuest().notifyEvent("QM" + _id + "", null, player);
			}
		}
	}

}