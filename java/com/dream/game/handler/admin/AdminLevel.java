package com.dream.game.handler.admin;

import com.dream.game.access.gmHandler;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemChatChannelId;

public class AdminLevel extends gmHandler
{
	private static final String[] commands =
	{
		"remlevel",
		"addlevel",
		"setlevel"
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
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Not specified level");
			return;
		}

		final String command = params[0];

		if (command.equals("addlevel") || command.equals("setlevel") || command.equals("remlevel"))
		{
			int reslevel = 0;
			int curlevel = 0;
			long xpcur = 0;
			long xpres = 0;
			int lvl = 0;

			try
			{
				lvl = Integer.parseInt(params[1]);
			}
			catch (Exception e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Not specified level");
				return;
			}

			L2Object target = admin.getTarget();
			if (target == null)
			{
				target = admin;
			}

			if (target instanceof L2Playable && lvl > 0)
			{
				L2Playable player = (L2Playable) target;

				curlevel = player.getLevel();
				reslevel = command.equals("addlevel") ? curlevel + lvl : command.equals("remlevel") ? curlevel - lvl : lvl;

				try
				{
					xpcur = player.getStat().getExp();
					xpres = player.getStat().getExpForLevel(reslevel);

					if (xpcur > xpres)
					{
						player.getStat().removeExp(xpcur - xpres);
					}
					else
					{
						player.getStat().addExp(xpres - xpcur);
					}

				}
				catch (Exception e)
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "The level is set incorrectly");
					return;
				}
			}
			else
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Not specified level");
				return;
			}
		}
	}
}