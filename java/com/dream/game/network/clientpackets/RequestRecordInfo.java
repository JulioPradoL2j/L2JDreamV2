package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.UserInfo;

public final class RequestRecordInfo extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
			return;

		activeChar.getKnownList().updateKnownObjects();
		activeChar.sendPacket(new UserInfo(activeChar));
	}

}