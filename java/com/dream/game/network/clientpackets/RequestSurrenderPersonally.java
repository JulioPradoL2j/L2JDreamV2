package com.dream.game.network.clientpackets;

import org.apache.log4j.Logger;

import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.model.L2Clan;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;

public class RequestSurrenderPersonally extends L2GameClientPacket
{
	public static Logger _log = Logger.getLogger(RequestSurrenderPersonally.class.getName());
	String _pledgeName;
	L2Clan _clan;
	L2PcInstance _activeChar;

	@Override
	protected void readImpl()
	{
		_pledgeName = readS();
	}

	@Override
	protected void runImpl()
	{
		_activeChar = getClient().getActiveChar();
		if (_activeChar == null)
			return;
		_log.info("RequestSurrenderPersonally by " + getClient().getActiveChar().getName() + " with " + _pledgeName);
		_clan = getClient().getActiveChar().getClan();
		L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);

		if (_clan == null)
			return;

		if (clan == null)
			return;

		if (!_clan.isAtWarWith(clan.getClanId()) || _activeChar.wantsPeace())
		{
			_activeChar.sendPacket(SystemMessageId.FAILED_TO_PERSONALLY_SURRENDER);
			return;
		}

		_activeChar.setWantsPeace(true);
		_activeChar.deathPenalty(false, false);
		_activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_PERSONALLY_SURRENDERED_TO_THE_S1_CLAN).addString(_pledgeName));
		ClanTable.getInstance().checkSurrender(_clan, clan);
	}

}