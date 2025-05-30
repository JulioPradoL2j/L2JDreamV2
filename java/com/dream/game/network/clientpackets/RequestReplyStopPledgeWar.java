package com.dream.game.network.clientpackets;

import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;

public class RequestReplyStopPledgeWar extends L2GameClientPacket
{
	private int _answer;

	@Override
	protected void readImpl()
	{
		readS();
		_answer = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		L2PcInstance requestor = activeChar.getActiveRequester();
		if (requestor == null)
			return;

		if (_answer == 1)
		{
			ClanTable.getInstance().deleteClansWars(requestor.getClanId(), activeChar.getClanId());
		}
		else
		{
			requestor.sendPacket(SystemMessageId.REQUEST_TO_END_WAR_HAS_BEEN_DENIED);
		}

		activeChar.setActiveRequester(null);
		requestor.onTransactionResponse();
	}

}