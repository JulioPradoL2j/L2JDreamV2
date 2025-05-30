package com.dream.game.network.clientpackets;

import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.model.L2Clan;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;

public class RequestSurrenderPledgeWar extends L2GameClientPacket
{
	String _pledgeName;
	L2Clan _clan;
	L2PcInstance activeChar;

	@Override
	protected void readImpl()
	{
		_pledgeName = readS();
	}

	@Override
	protected void runImpl()
	{
		activeChar = getClient().getActiveChar();
		L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
		_clan = activeChar.getClan();

		if (activeChar == null)
			return;
		if (!activeChar.isClanLeader())
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		if (_clan == null)
			return;
		if (clan == null)
			return;
		if (!_clan.isAtWarWith(clan.getClanId()))
		{
			activeChar.sendPacket(SystemMessageId.NOT_INVOLVED_IN_WAR);
			return;
		}

		activeChar.deathPenalty(false, false, false);
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN).addString(_pledgeName));
		ClanTable.getInstance().deleteClansWars(_clan.getClanId(), clan.getClanId());
	}

}