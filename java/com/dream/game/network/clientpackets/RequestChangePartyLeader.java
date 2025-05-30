package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;

public class RequestChangePartyLeader extends L2GameClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
			return;
		if (activeChar.isInParty() && activeChar.getParty().isLeader(activeChar))
		{
			activeChar.getParty().changePartyLeader(_name);
		}
	}

}