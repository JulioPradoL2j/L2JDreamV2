package com.dream.game.network.clientpackets;

import com.dream.game.handler.IUserCommandHandler;
import com.dream.game.handler.UserCommandHandler;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.util.FloodProtector;
import com.dream.game.util.FloodProtector.Protected;

public class RequestUserCommand extends L2GameClientPacket
{
	private int _command;

	@Override
	protected void readImpl()
	{
		_command = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();

		if (player == null)
			return;

		IUserCommandHandler handler = UserCommandHandler.getInstance().getUserCommandHandler(_command);

		if (handler == null)
			return;
		if (!FloodProtector.tryPerformAction(player, Protected.USER_CMD))
		{

		}
		else
		{
			handler.useUserCommand(_command, player);
		}
	}

}