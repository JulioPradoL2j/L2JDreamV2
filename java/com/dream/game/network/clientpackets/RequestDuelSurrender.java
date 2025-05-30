package com.dream.game.network.clientpackets;

import com.dream.game.manager.DuelManager;

public final class RequestDuelSurrender extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		DuelManager.getInstance().doSurrender(getClient().getActiveChar());
	}

}