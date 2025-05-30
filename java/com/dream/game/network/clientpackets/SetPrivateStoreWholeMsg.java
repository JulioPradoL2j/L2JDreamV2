package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.ExPrivateStoreSetWholeMsg;

public class SetPrivateStoreWholeMsg extends L2GameClientPacket
{
	private String _msg;

	@Override
	protected void readImpl()
	{
		_msg = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null || player.getSellList() == null)
			return;

		player.getSellList().setTitle(_msg);
		sendPacket(new ExPrivateStoreSetWholeMsg(player));
	}

}