package com.dream.game.network.clientpackets;

import java.util.List;

import com.dream.game.manager.PartyRoomManager;
import com.dream.game.model.L2PartyRoom;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;

public class RequestPartyMatchDetail extends L2GameClientPacket
{
	private int _roomId;
	private int _region;

	@Override
	protected void readImpl()
	{
		_roomId = readD();
		_region = readD();
		readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (activeChar.getPartyRoom() != null || activeChar.getParty() != null)
		{
			activeChar.sendPacket(SystemMessageId.PARTY_ROOM_FORBIDDEN);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		activeChar.setPartyMatchingRegion(_region);
		activeChar.setPartyMatchingShowClass(false);

		if (_roomId > 0)
		{
			L2PartyRoom room = PartyRoomManager.getInstance().getPartyRoom(_roomId);
			L2PartyRoom.tryJoin(activeChar, room, false);
		}
		else
		{
			List<L2PartyRoom> list = PartyRoomManager.getInstance().getRooms(activeChar);
			for (L2PartyRoom room : list)
				if (room.canJoin(activeChar))
				{
					room.addMember(activeChar);
					break;
				}
		}

		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	}

}