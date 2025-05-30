package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;

public class RequestListPartyMatchingWaitingRoom extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _page;
	@SuppressWarnings("unused")
	private boolean _showAll;
	private int _minLevel;
	private int _maxLevel;

	@Override
	protected void readImpl()
	{
		_page = readD();
		_minLevel = readD();
		_maxLevel = readD();
		_showAll = readD() == 1;
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		if (_minLevel < 1)
		{
			_minLevel = 1;
		}
		else if (_minLevel > 85)
		{
			_minLevel = 85;
		}
		if (_maxLevel < _minLevel)
		{
			_maxLevel = _minLevel;
		}
		else if (_maxLevel > 85)
		{
			_maxLevel = 85;
		}
	}

}