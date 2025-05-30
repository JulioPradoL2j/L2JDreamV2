package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;

public class ObserverReturn extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		if (activeChar.inObserverMode())
		{
			activeChar.leaveObserverMode();
		}
	}

}