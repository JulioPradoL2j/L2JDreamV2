package com.dream.game.network.clientpackets;

import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;

public class RequestReplyStartPledgeWar extends L2GameClientPacket
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
			ClanTable.getInstance().storeClansWars(requestor.getClanId(), activeChar.getClanId());
		}
		else
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_WAR_PROCLAMATION_HAS_BEEN_REFUSED).addString(activeChar.getClan().getName()));
		}
		activeChar.setActiveRequester(null);
		requestor.onTransactionResponse();
	}

}