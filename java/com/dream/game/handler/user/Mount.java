package com.dream.game.handler.user;

import com.dream.game.handler.IUserCommandHandler;
import com.dream.game.model.actor.instance.L2PcInstance;

public class Mount implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		61
	};

	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}

	@Override
	public synchronized boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (id != COMMAND_IDS[0])
			return false;
		return activeChar.mountPlayer(activeChar.getPet());
	}
}