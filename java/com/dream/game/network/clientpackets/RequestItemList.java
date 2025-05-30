package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.ItemList;

public class RequestItemList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		if (getClient() != null && getClient().getActiveChar() != null && !getClient().getActiveChar().isInvetoryDisabled())
		{
			L2PcInstance pc = getClient().getActiveChar();
			if (pc.getTrading())
			{
				pc.cancelActiveTrade();
				pc.setTrading(false);
			}
			sendPacket(new ItemList(getClient().getActiveChar(), true));
		}
	}

}