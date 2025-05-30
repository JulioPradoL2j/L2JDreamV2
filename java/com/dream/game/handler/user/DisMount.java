package com.dream.game.handler.user;

import com.dream.game.handler.IUserCommandHandler;
import com.dream.game.model.actor.instance.L2PcInstance;

public class DisMount implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		62
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

		if (activeChar.isRentedPet())
		{
			activeChar.stopRentPet();
		}
		else if (activeChar.isMounted())
		{
			activeChar.dismount();
		}

		return true;
	}
}