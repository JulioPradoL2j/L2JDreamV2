package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;

public class RequestOustPartyMember extends L2GameClientPacket
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
			if (activeChar.getParty().isInDimensionalRift() && !activeChar.getParty().getDimensionalRift().getRevivedAtWaitingRoom().contains(activeChar))
			{
				activeChar.sendPacket(SystemMessageId.COULD_NOT_OUST_FROM_PARTY);
			}
			else
			{
				activeChar.getParty().removePartyMember(_name, true);
			}
	}

}