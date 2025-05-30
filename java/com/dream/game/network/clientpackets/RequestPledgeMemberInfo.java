package com.dream.game.network.clientpackets;

import com.dream.game.model.L2Clan;
import com.dream.game.model.L2ClanMember;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.PledgeReceiveMemberInfo;

public class RequestPledgeMemberInfo extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _pledgeType;
	private String _player;

	@Override
	protected void readImpl()
	{
		_pledgeType = readD();
		_player = readS();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		// do we need powers to do that??
		final L2Clan clan = activeChar.getClan();
		if (clan == null)
			return;

		final L2ClanMember member = clan.getClanMember(_player);
		if (member == null)
			return;

		activeChar.sendPacket(new PledgeReceiveMemberInfo(member));
	}

}