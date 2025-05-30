package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.PrivateStoreMsgBuy;

public class SetPrivateStoreMsgBuy extends L2GameClientPacket
{
	private String _storeMsg;

	@Override
	protected void readImpl()
	{
		_storeMsg = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null || player.getBuyList() == null)
			return;

		player.getBuyList().setTitle(_storeMsg);
		player.sendPacket(new PrivateStoreMsgBuy(player));
	}

}