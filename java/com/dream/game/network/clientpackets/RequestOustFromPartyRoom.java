package com.dream.game.network.clientpackets;

import com.dream.game.model.L2Party;
import com.dream.game.model.L2PartyRoom;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;

public class RequestOustFromPartyRoom extends L2GameClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		L2PcInstance target = L2World.getInstance().findPlayer(_objectId);
		if (target == null || target == activeChar)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2Party party = target.getParty();
		if (party != null && party.isInDimensionalRift() && !party.getDimensionalRift().getRevivedAtWaitingRoom().contains(activeChar))
		{
			activeChar.sendPacket(SystemMessageId.COULD_NOT_OUST_FROM_PARTY);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2PartyRoom room = activeChar.getPartyRoom();
		if (room != null && room.getLeader() == activeChar)
			if (party != null)
			{
				party.removePartyMember(target, true);
			}
			else
			{
				room.removeMember(target, true);
			}

		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	}

}