package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;

public class RequestShortCutDel extends L2GameClientPacket
{
	private int _slot, _page;

	@Override
	protected void readImpl()
	{
		int id = readD();
		_slot = id % 12;
		_page = id / 12;
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		if (_page > 9 || _page < 0)
			return;

		activeChar.deleteShortCut(_slot, _page);
	}

}