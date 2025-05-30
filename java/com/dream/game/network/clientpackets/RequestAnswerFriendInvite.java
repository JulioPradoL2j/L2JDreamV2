package com.dream.game.network.clientpackets;

import com.dream.game.model.L2FriendList;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.FriendList;
import com.dream.game.network.serverpackets.SystemMessage;

public class RequestAnswerFriendInvite extends L2GameClientPacket
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

		L2PcInstance requestor = activeChar.getActiveRequester();

		if (requestor == null)
		{
			activeChar.sendPacket(SystemMessageId.THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME);
			return;
		}
		if (_response == 1)
		{
			requestor.sendPacket(SystemMessageId.YOU_HAVE_SUCCEEDED_INVITING_FRIEND);

			L2FriendList.addToFriendList(requestor, activeChar);
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ADDED_TO_FRIENDS).addString(activeChar.getName()));
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_JOINED_AS_FRIEND).addString(requestor.getName()));
		}
		else
		{
			requestor.sendPacket(SystemMessageId.FAILED_TO_INVITE_A_FRIEND);
		}

		activeChar.setActiveRequester(null);
		requestor.onTransactionResponse();

		activeChar.sendPacket(new FriendList(activeChar));
		requestor.sendPacket(new FriendList(requestor));
	}

}