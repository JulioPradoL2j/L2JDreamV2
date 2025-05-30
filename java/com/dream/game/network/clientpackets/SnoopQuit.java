package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;

public class SnoopQuit extends L2GameClientPacket
{
	private int _snoopId;

	@Override
	protected void readImpl()
	{
		_snoopId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		L2PcInstance target = L2World.getInstance().getPlayer(_snoopId);

		if (target == null)
			return;

		player.removeSnooped(target);
		target.removeSnooper(player);
		player.sendMessage("Listen to player " + target.getName() + " cancelled.");
	}

}