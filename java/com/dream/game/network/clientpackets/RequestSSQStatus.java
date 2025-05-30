package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.sevensigns.SevenSigns;
import com.dream.game.network.serverpackets.SSQStatus;

public class RequestSSQStatus extends L2GameClientPacket
{
	private int _page;

	@Override
	protected void readImpl()
	{
		_page = readC();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if ((SevenSigns.getInstance().isSealValidationPeriod() || SevenSigns.getInstance().isCompResultsPeriod()) && _page == 4)
			return;

		activeChar.sendPacket(new SSQStatus(activeChar, _page));
	}

}