package com.dream.game.network.clientpackets;

import com.dream.game.manager.PartyRoomManager;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.ActionFailed;

public class RequestExitPartyMatchingWaitingRoom extends L2GameClientPacket
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

		PartyRoomManager.getInstance().removeFromWaitingList(activeChar);

		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	}

}