package com.dream.game.network.clientpackets;

import com.dream.game.model.L2Party;
import com.dream.game.model.L2PartyRoom;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.JoinParty;

public class RequestAnswerJoinParty extends L2GameClientPacket
{
	private int _response;

	@Override
	protected void readImpl()
	{
		_response = readD();
	}

	@Override
	protected void runImpl()
	{
		if (getClient().getActiveChar() == null || getClient().getActiveChar().getParty() != null)
			return;

		L2PcInstance requestor = getClient().getActiveChar().getActiveRequester();
		if (requestor == null)
			return;

		if (getClient().getActiveChar() != null)
		{
			requestor.sendPacket(new JoinParty(_response));

			if (_response == 1)
			{
				if (requestor.getParty() != null)
					if (requestor.getParty().getMemberCount() >= 9)
					{
						getClient().getActiveChar().sendPacket(SystemMessageId.PARTY_FULL);
						requestor.sendPacket(SystemMessageId.PARTY_FULL);
						return;
					}
				getClient().getActiveChar().joinParty(requestor.getParty());
			}
			else
			{
				requestor.sendPacket(SystemMessageId.PLAYER_DECLINED);

				L2Party party = requestor.getParty();
				if (party != null && party.getMemberCount() == 1)
				{
					L2PartyRoom room = party.getPartyRoom();
					if (room != null)
					{
						room.setParty(null);
					}
					party.setPartyRoom(null);
					requestor.setParty(null);
				}

			}
			if (requestor.getParty() != null)
			{
				requestor.getParty().decreasePendingInvitationNumber();
			}

			getClient().getActiveChar().setActiveRequester(null);
			requestor.onTransactionResponse();
		}
	}

}