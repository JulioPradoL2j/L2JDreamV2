package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;

public class RequestWithDrawalParty extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		if (player.isArenaProtection())
		{
			player.sendMessage("You can't exit party when you are in Tournament.");
			return;
		}
		if (player.isInParty())
			if (player.getParty().isInDimensionalRift() && !player.getParty().getDimensionalRift().getRevivedAtWaitingRoom().contains(player))
			{
				player.sendMessage("You can not leave the group in Dimensional rift.");
			}
			else
			{
				player.getParty().removePartyMember(player, false);
			}
	}
}