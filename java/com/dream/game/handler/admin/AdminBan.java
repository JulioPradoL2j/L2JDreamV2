package com.dream.game.handler.admin;

import com.dream.Message;
import com.dream.game.access.gmHandler;
import com.dream.game.manager.BanManager;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;

public class AdminBan extends gmHandler
{
	private static final String[] commands =
	{
		"ban",
		"unban",
		"banchar",
		"unbanchar",
		"gmacc"
	};

	@Override
	public String[] getCommandList()
	{
		return commands;
	}

	@Override
	public void runCommand(L2PcInstance admin, String... params)
	{
		if (admin == null)
			return;

		if (params.length < 2)
		{
			admin.sendMessage(Message.getMessage(admin, Message.MessageId.MSG_SET_ALL_ARG));
			return;
		}

		String command = params[0];
		L2PcInstance player = L2World.getInstance().getPlayer(params[1]);

		if (command.equals("ban"))
		{
			if (player != null)
			{
				if (!BanManager.getInstance().banAccount(admin, player))
				{
					admin.sendMessage(Message.getMessage(admin, Message.MessageId.MSG_ERROR_TRY_LATER));
				}
			}
			else if (!BanManager.getInstance().banAccount(admin, params[1]))
			{
				admin.sendMessage(Message.getMessage(admin, Message.MessageId.MSG_ERROR_TRY_LATER));
			}
			return;
		}
		else if (command.equals("unban"))
		{
			if (!BanManager.getInstance().unBanAccount(admin, params[1]))
			{
				admin.sendMessage(Message.getMessage(admin, Message.MessageId.MSG_ERROR_TRY_LATER));
			}
			return;
		}
		else if (command.equals("banchar"))
		{
			if (!BanManager.getInstance().banChar(admin, params[1]))
			{
				admin.sendMessage(Message.getMessage(admin, Message.MessageId.MSG_ERROR_TRY_LATER));
			}
			return;
		}
		else if (command.equals("unbanchar"))
		{
			if (!BanManager.getInstance().unBanChar(admin, params[1]))
			{
				admin.sendMessage(Message.getMessage(admin, Message.MessageId.MSG_ERROR_TRY_LATER));
			}
			return;
		}
		else if (command.equals("gmacc"))
		{
			if (!BanManager.getInstance().gmAccess(admin, params[1]))
			{
				admin.sendMessage(Message.getMessage(admin, Message.MessageId.MSG_ERROR_TRY_LATER));
			}
			return;
		}
	}
}