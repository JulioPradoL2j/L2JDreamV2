package com.dream.game.handler.admin;

import com.dream.game.access.gmHandler;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.NpcHtmlMessage;

public class AdminExpSp extends gmHandler
{
	private static final String[] commands =
	{
		"add_exp_sp_to_character",
		"add_exp_sp",
		"remove_exp_sp"
	};

	private static void addExpSp(L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		if (target == null)
		{
			target = activeChar;
		}

		if (!(target instanceof L2PcInstance))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}

		L2PcInstance player = (L2PcInstance) target;

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/menus/submenus/expsp_menu.htm");
		adminReply.replace("%name%", player.getName());
		adminReply.replace("%xp%", String.valueOf(player.getExp()));
		adminReply.replace("%sp%", String.valueOf(player.getSp()));
		activeChar.sendPacket(adminReply);
	}

	private static boolean adminAddExpSp(L2PcInstance activeChar, long exp, long sp)
	{
		L2Object target = activeChar.getTarget();
		if (target == null)
		{
			target = activeChar;
		}

		if (!(target instanceof L2PcInstance))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return false;
		}

		L2PcInstance player = (L2PcInstance) target;
		if (exp != 0 || sp != 0)
		{
			player.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Gm added you " + exp + " xp and " + sp + " sp");
			player.addExpAndSp(exp, (int) sp);
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Added " + exp + " xp and " + sp + " sp the player " + player.getName());
		}
		return true;
	}

	private static boolean adminRemoveExpSP(L2PcInstance activeChar, long exp, long sp)
	{
		L2Object target = activeChar.getTarget();
		if (target == null)
		{
			target = activeChar;
		}

		if (!(target instanceof L2PcInstance))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return false;
		}

		L2PcInstance player = (L2PcInstance) target;
		if (exp != 0 || sp != 0)
		{
			player.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Gm has deleted you " + exp + " xp and " + sp + " sp");
			player.removeExpAndSp(exp, (int) sp);
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Removed " + exp + " xp and " + sp + " sp the player " + player.getName());
		}
		return true;
	}

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

		if (command.equals("add_exp_sp"))
		{
			try
			{
				long exp = Long.parseLong(params[1]);
				long sp = Long.parseLong(params[2]);
				if (!adminAddExpSp(admin, exp, sp))
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //add_exp_sp exp sp");
				}
			}
			catch (Exception e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //add_exp_sp exp sp");
			}
		}
		else if (command.equals("remove_exp_sp"))
		{
			try
			{
				long exp = Long.parseLong(params[1]);
				long sp = Long.parseLong(params[2]);
				if (!adminRemoveExpSP(admin, exp, sp))
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //remove_exp_sp exp sp");
				}
			}
			catch (Exception e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //remove_exp_sp exp sp");
			}
		}
		addExpSp(admin);
	}
}