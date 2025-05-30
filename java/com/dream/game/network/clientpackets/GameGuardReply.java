package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;

public class GameGuardReply extends L2GameClientPacket
{
	private final int[] _reply = new int[4];
	
	@Override
	protected void readImpl()
	{
		_reply[0] = readD();
		_reply[1] = readD();
		_reply[2] = readD();
		_reply[3] = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		getClient().setGameGuardOk(true);
	}
	
}