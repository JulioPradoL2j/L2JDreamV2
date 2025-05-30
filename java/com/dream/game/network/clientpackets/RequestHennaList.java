package com.dream.game.network.clientpackets;

import com.dream.game.datatables.sql.HennaTreeTable;
import com.dream.game.model.actor.instance.L2HennaInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.HennaEquipList;

public class RequestHennaList extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _unknown;

	@Override
	protected void readImpl()
	{
		_unknown = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		L2HennaInstance[] henna = HennaTreeTable.getInstance().getAvailableHenna(activeChar.getClassId());
		if (henna == null)
			return;
		activeChar.sendPacket(new HennaEquipList(activeChar, henna));
	}

}