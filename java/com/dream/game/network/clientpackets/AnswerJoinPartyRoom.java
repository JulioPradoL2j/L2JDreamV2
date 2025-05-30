package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.Shutdown;
import com.dream.game.model.L2PartyRoom;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;

public class AnswerJoinPartyRoom extends L2GameClientPacket
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
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_TRANSACTION && Shutdown.getCounterInstance() != null && Shutdown.getCounterInstance().getCountdown() <= Config.SAFE_REBOOT_TIME)
		{
			activeChar.sendPacket(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2PcInstance requester = activeChar.getActiveRequester();
		if (requester == null)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (_response == 1)
		{
			L2PartyRoom.tryJoin(activeChar, requester.getPartyRoom(), true);
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.PARTY_MATCHING_REQUEST_NO_RESPONSE);
		}

		activeChar.setActiveRequester(null);
		requester.onTransactionResponse();

		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	}

}