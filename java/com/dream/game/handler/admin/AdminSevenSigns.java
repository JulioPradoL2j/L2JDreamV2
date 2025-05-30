package com.dream.game.handler.admin;

import com.dream.game.access.gmHandler;
import com.dream.game.manager.AutoSpawnManager;
import com.dream.game.manager.AutoSpawnManager.AutoSpawnInstance;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.sevensigns.SevenSigns;
import com.dream.game.network.SystemChatChannelId;

public class AdminSevenSigns extends gmHandler
{
	private static final String[] commands =
	{
		"ss_period_change",
		"mammon_find"
	};
	private final AutoSpawnManager autospawn = AutoSpawnManager.getInstance();
	private AutoSpawnInstance blackSpawnInst = null;
	private AutoSpawnInstance merchSpawnInst = null;
	private int teleportIndex = -1;

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

		String command = params[0];

		if (command.equals("ss_period_change"))
		{
			SevenSigns.getInstance().changePeriodManualy();
			return;
		}
		else if (command.equals("mammon_find"))
		{

			if (!SevenSigns.getInstance().isSealValidationPeriod())
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "In the current period, Mammon is not active");
				return;
			}

			blackSpawnInst = autospawn.getAutoSpawnInstance(SevenSigns.MAMMON_BLACKSMITH_ID, false);
			merchSpawnInst = autospawn.getAutoSpawnInstance(SevenSigns.MAMMON_MERCHANT_ID, false);

			try
			{
				teleportIndex = Integer.parseInt(params[1]);
			}
			catch (Exception NumberFormatException)
			{
			}

			if (blackSpawnInst != null)
			{
				L2Npc[] blackInst = blackSpawnInst.getNPCInstanceList();
				if (blackInst.length > 0)
				{
					int x1 = blackInst[0].getX(), y1 = blackInst[0].getY(), z1 = blackInst[0].getZ();
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Blacksmith of Mammon: " + x1 + " " + y1 + " " + z1);
					if (teleportIndex == 1)
					{
						admin.teleToLocation(x1, y1, z1, true);
					}
				}
				else
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Blacksmith of Mammon: not found");
				}
			}
			else
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Blacksmith of Mammon: not found");
			}

			if (merchSpawnInst != null)
			{
				L2Npc[] merchInst = merchSpawnInst.getNPCInstanceList();
				if (merchInst.length > 0)
				{
					int x2 = merchInst[0].getX(), y2 = merchInst[0].getY(), z2 = merchInst[0].getZ();
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Merchant of Mammon: " + x2 + " " + y2 + " " + z2);
					if (teleportIndex == 2)
					{
						admin.teleToLocation(x2, y2, z2, true);
					}
				}
				else
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Merchant of Mammon: not found");
				}
			}
			else
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Merchant of Mammon: not found");
			}
		}
		else
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Cmd: '" + command + "'");
		}
	}
}