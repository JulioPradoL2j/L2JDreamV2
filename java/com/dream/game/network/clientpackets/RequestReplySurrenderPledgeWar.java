package com.dream.game.network.clientpackets;

import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.model.actor.instance.L2PcInstance;

public class RequestReplySurrenderPledgeWar extends L2GameClientPacket
{
	int _answer;

	@Override
	protected void readImpl()
	{
		readS();
		_answer = readD();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		final L2PcInstance requestor = activeChar.getActiveRequester();
		if (requestor == null)
			return;

		if (_answer == 1)
		{
			requestor.deathPenalty(false, false, false);
			ClanTable.getInstance().deleteClansWars(requestor.getClanId(), activeChar.getClanId());
		}

		activeChar.onTransactionRequest(requestor);
	}

}