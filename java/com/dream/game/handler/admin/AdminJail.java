package com.dream.game.handler.admin;

import com.dream.game.access.gmHandler;
import com.dream.game.manager.BanManager;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemChatChannelId;

public class AdminJail extends gmHandler
{
	private static final String[] commands =
	{
		"jail",
		"unjail"
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

		final String command = params[0];
		int time = -1;
		L2PcInstance player = null;

		if (params.length > 1)
		{
			player = L2World.getInstance().getPlayer(params[1]);
			if (params.length > 2)
			{
				time = Integer.parseInt(params[2]);
			}
		}
		else
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Parameters are incorrect.");
			return;
		}

		if (command.startsWith("jail"))
		{
			if (player != null)
			{
				if (!BanManager.getInstance().jailPlayer(admin, player, time, false))
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "An error occurred, please try again.");
				}
			}
			else if (!BanManager.getInstance().jailPlayer(admin, params[1], time))
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "An error occurred, please try again.");
			}
			return;
		}
		else if (command.startsWith("unjail"))
		{
			if (player != null)
			{
				if (!BanManager.getInstance().unJailPlayer(admin, player))
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "An error occurred, please try again.");
				}
			}
			else if (!BanManager.getInstance().unJailPlayer(admin, params[1]))
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "An error occurred, please try again.");
			}
			return;
		}
	}
}