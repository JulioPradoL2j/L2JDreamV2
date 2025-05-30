package com.dream.game.network.clientpackets;

import com.dream.game.model.L2Party;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.ExMPCCShowPartyMemberInfo;

public final class RequestExMPCCShowPartyMembersInfo extends L2GameClientPacket
{
	private int _leaderId;

	@Override
	protected void readImpl()
	{
		_leaderId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();

		if (player == null || player.getParty() == null || player.getParty().getCommandChannel() == null)
			return;

		for (L2Party party : player.getParty().getCommandChannel().getPartys())
			if (party.getLeader().getObjectId() == _leaderId)
			{
				player.sendPacket(new ExMPCCShowPartyMemberInfo(party));
				return;
			}
	}

}