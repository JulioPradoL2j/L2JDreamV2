package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.util.Util;

public class RequestRecipeShopMessageSet extends L2GameClientPacket
{
	private static final int MAX_MSG_LENGTH = 29;
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		if (_name != null && _name.length() > MAX_MSG_LENGTH)
		{
			Util.handleIllegalPlayerAction(player, player.getName() + " tried to overflow recipe shop message", Config.DEFAULT_PUNISH);
			return;
		}

		if (player.getCreateList() != null)
		{
			player.getCreateList().setStoreName(_name);
		}

	}

}