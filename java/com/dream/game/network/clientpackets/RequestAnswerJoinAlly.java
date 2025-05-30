package com.dream.game.network.clientpackets;

import com.dream.game.model.L2Clan;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;

public class RequestAnswerJoinAlly extends L2GameClientPacket
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
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		L2PcInstance requestor = activeChar.getRequest().getPartner();

		if (requestor == null)
			return;
		if (_response == 0)
		{
			activeChar.sendPacket(SystemMessageId.YOU_DID_NOT_RESPOND_TO_ALLY_INVITATION);
			requestor.sendPacket(SystemMessageId.NO_RESPONSE_TO_ALLY_INVITATION);
		}
		else
		{
			L2Clan clan = requestor.getClan();

			if (!(requestor.getRequest().getRequestPacket() instanceof RequestJoinAlly))
				return;
			if (clan.checkAllyJoinCondition(requestor, activeChar))
			{
				requestor.sendPacket(SystemMessageId.YOU_INVITED_FOR_ALLIANCE);
				activeChar.sendPacket(SystemMessageId.YOU_ACCEPTED_ALLIANCE);
				activeChar.getClan().setAllyId(clan.getAllyId());
				activeChar.getClan().setAllyName(clan.getAllyName());
				activeChar.getClan().setAllyPenaltyExpiryTime(0, 0);
				activeChar.getClan().setAllyCrestId(clan.getAllyCrestId());
				activeChar.getClan().updateClanInDB();
				activeChar.getClan().setAllyCrestId(requestor.getClan().getAllyCrestId());
				for (L2PcInstance member : activeChar.getClan().getOnlineMembers(0))
				{
					member.broadcastUserInfo();
				}
			}
		}
		activeChar.getRequest().onRequestResponse();
	}

}