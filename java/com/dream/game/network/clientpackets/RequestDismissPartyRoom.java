package com.dream.game.network.clientpackets;

import com.dream.game.manager.PartyRoomManager;
import com.dream.game.model.L2PartyRoom;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.ActionFailed;

public class RequestDismissPartyRoom extends L2GameClientPacket
{
	private int _roomId;

	@Override
	protected void readImpl()
	{
		_roomId = readD();
		readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		L2PartyRoom room = activeChar.getPartyRoom();
		if (room != null && room.getId() == _roomId && room.getLeader() == activeChar)
		{
			PartyRoomManager.getInstance().removeRoom(_roomId);
		}

		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	}

}