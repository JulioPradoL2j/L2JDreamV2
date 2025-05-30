package com.dream.game.network.clientpackets;

import com.dream.game.datatables.xml.HennaTable;
import com.dream.game.model.actor.instance.L2HennaInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.HennaItemInfo;
import com.dream.game.templates.item.L2Henna;

public class RequestHennaItemInfo extends L2GameClientPacket
{
	private int _symbolId;

	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		L2Henna template = HennaTable.getInstance().getTemplate(_symbolId);

		if (template == null)
			return;

		L2HennaInstance temp = new L2HennaInstance(template);
		activeChar.sendPacket(new HennaItemInfo(temp, activeChar));
	}

}