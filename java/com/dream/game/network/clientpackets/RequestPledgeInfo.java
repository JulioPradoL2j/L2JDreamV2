package com.dream.game.network.clientpackets;

import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.model.L2Clan;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.PledgeInfo;

public class RequestPledgeInfo extends L2GameClientPacket
{
	private int _clanId;

	@Override
	protected void readImpl()
	{
		_clanId = readD();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		final L2Clan clan = ClanTable.getInstance().getClan(_clanId);
		if (clan == null)
			return;

		activeChar.sendPacket(new PledgeInfo(clan));
	}

}