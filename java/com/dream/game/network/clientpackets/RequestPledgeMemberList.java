package com.dream.game.network.clientpackets;

import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Clan.SubPledge;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.PledgeShowMemberListAll;

public class RequestPledgeMemberList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		final L2Clan clan = activeChar.getClan();
		if (clan == null)
			return;

		activeChar.sendPacket(new PledgeShowMemberListAll(clan, 0));

		for (SubPledge sp : clan.getAllSubPledges())
		{
			activeChar.sendPacket(new PledgeShowMemberListAll(clan, sp.getId()));
		}
	}

}