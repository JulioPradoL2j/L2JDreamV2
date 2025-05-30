package com.dream.game.network.clientpackets;

import com.dream.game.manager.PartyRoomManager;
import com.dream.game.model.L2Party;
import com.dream.game.model.L2PartyRoom;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;

public class RequestWithdrawPartyRoom extends L2GameClientPacket
{
	private int _roomId;
	@SuppressWarnings("unused")
	private int _data2;

	@Override
	protected void readImpl()
	{
		_roomId = readD();
		_data2 = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		L2Party party = activeChar.getParty();
		if (party != null && party.isInDimensionalRift() && !party.getDimensionalRift().getRevivedAtWaitingRoom().contains(activeChar))
		{
			activeChar.sendPacket(SystemMessageId.COULD_NOT_LEAVE_PARTY);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2PartyRoom room = activeChar.getPartyRoom();
		if (room != null && room.getId() == _roomId)
			if (room.getLeader() == activeChar)
			{
				PartyRoomManager.getInstance().removeRoom(_roomId);
			}
			else if (party != null)
			{
				party.removePartyMember(activeChar, false);
			}
			else
			{
				room.removeMember(activeChar, false);
			}
	}

}