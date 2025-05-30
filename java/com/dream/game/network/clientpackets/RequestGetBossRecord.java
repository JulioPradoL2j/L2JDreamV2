package com.dream.game.network.clientpackets;

import java.util.Map;
import java.util.logging.Logger;

import com.dream.game.manager.RaidPointsManager;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.ExGetBossRecord;

public class RequestGetBossRecord extends L2GameClientPacket
{
	protected static final Logger _log = Logger.getLogger(RequestGetBossRecord.class.getName());
	private int _bossId;

	@Override
	protected void readImpl()
	{
		_bossId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (_bossId != 0)
		{
			_log.info("RequestGetBossRecord " + _bossId + " ActiveChar: " + activeChar);
		}
		int points = RaidPointsManager.getPointsByOwnerId(activeChar.getObjectId());
		int ranking = RaidPointsManager.calculateRanking(activeChar.getObjectId());

		Map<Integer, Integer> list = RaidPointsManager.getList(activeChar);
		activeChar.sendPacket(new ExGetBossRecord(ranking, points, list));
	}

}