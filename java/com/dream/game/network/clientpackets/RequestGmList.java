package com.dream.game.network.clientpackets;

import com.dream.game.datatables.GmListTable;

public class RequestGmList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		if (getClient().getActiveChar() == null)
			return;
		GmListTable.getInstance().sendListToPlayer(getClient().getActiveChar());
	}

}