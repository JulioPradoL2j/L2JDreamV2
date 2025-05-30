package com.dream.game.handler.user;

import com.dream.game.GameTimeController;
import com.dream.game.handler.IUserCommandHandler;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;

public class Time implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		77
	};

	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}

	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (COMMAND_IDS[0] != id)
			return false;

		int time = GameTimeController.getGameTime();
		String hour = "" + time / 60;
		String minute;

		if (time % 60 < 10)
		{
			minute = "0" + time % 60;
		}
		else
		{
			minute = "" + time % 60;
		}

		if (GameTimeController.isNowNight())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CURRENT_TIME_S1_S2_PM).addString(hour).addString(minute));
		}
		else
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CURRENT_TIME_S1_S2_AM).addString(hour).addString(minute));
		}
		return true;
	}
}