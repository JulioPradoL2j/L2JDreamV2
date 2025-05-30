package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.PartyMemberPosition;

public class Appearing extends L2GameClientPacket
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
		activeChar._inWorld = true;
		if (activeChar.isTeleporting())
		{
			activeChar.onTeleported();
		}

		activeChar.broadcastFullInfo();
		if (activeChar.getParty() != null)
		{
			activeChar.getParty().broadcastToPartyMembers(activeChar, new PartyMemberPosition(activeChar));
		}
	}

}