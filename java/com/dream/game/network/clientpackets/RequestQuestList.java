package com.dream.game.network.clientpackets;

import com.dream.game.network.serverpackets.QuestList;

public class RequestQuestList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		if (getClient().getActiveChar() != null)
		{
			sendPacket(new QuestList(getClient().getActiveChar()));
		}
	}

}