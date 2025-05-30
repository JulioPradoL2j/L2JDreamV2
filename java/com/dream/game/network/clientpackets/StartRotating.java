package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.StartRotation;

public class StartRotating extends L2GameClientPacket
{
	private int _degree;
	private int _side;

	@Override
	protected void readImpl()
	{
		_degree = readD();
		_side = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		activeChar.broadcastPacket(new StartRotation(activeChar.getObjectId(), _degree, _side, 0));
	}

}