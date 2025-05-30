package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;

public class ChangeMoveType2 extends L2GameClientPacket
{
	private boolean _typeRun;

	@Override
	protected void readImpl()
	{
		_typeRun = readD() == 1;
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		if (_typeRun)
		{
			player.setRunning();
		}
		else
		{
			player.setWalking();
		}
	}

}