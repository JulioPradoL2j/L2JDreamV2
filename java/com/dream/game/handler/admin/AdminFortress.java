package com.dream.game.handler.admin;

import com.dream.game.access.gmHandler;
import com.dream.game.manager.FortManager;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.siege.Fort;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.NpcHtmlMessage;

public class AdminFortress extends gmHandler
{
	private static final String[] commands =
	{
		"fortsiege",
		"add_fortattacker",
		"clear_fortsiege_list",
		"spawn_fortdoors",
		"endfortsiege",
		"startfortsiege",
		"setfort",
		"removefort"
	};

	private static void showFortSelectPage(L2PcInstance admin)
	{
		int i = 0;
		int total = 0;
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/menus/submenus/forts_menu.htm");
		StringBuilder cList = new StringBuilder();
		for (Fort fort : FortManager.getInstance().getForts())
		{
			if (fort != null)
			{
				String name = fort.getName();
				cList.append("<td fixwidth=90><a action=\"bypass -h admin_fortsiege " + String.valueOf(fort.getFortId()) + "\">" + name + "</a></td>");
				i++;
				total++;
			}
			if (i > 2)
			{
				cList.append("</tr><tr>");
				i = 0;
			}
		}
		adminReply.replace("%forts%", total > 0 ? cList.toString() : "<td><center>No forts</center></td>");
		admin.sendPacket(adminReply);
	}

	private static void showFortSiegePage(L2PcInstance admin, Fort fort)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/menus/submenus/fort_menu.htm");
		adminReply.replace("%fortName%", fort.getName());
		adminReply.replace("%fortId%", String.valueOf(fort.getFortId()));
		admin.sendPacket(adminReply);
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
		Fort fort = null;
		int fortId = 0;

		if (params.length > 1)
		{
			String val = params[1];
			try
			{
				fortId = Integer.parseInt(val);
				fort = FortManager.getInstance().getFortById(fortId);
			}
			catch (Exception e)
			{
				fort = FortManager.getInstance().getFort(val);
			}
		}

		if (fort == null)
		{
			showFortSelectPage(admin);
		}
		else
		{
			L2Object target = admin.getTarget();
			L2PcInstance player = null;
			if (target != null && target instanceof L2PcInstance)
			{
				player = (L2PcInstance) target;
			}

			if (command.equalsIgnoreCase("add_fortattacker"))
			{
				if (player == null)
				{
					admin.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				}
				else if (fort.getSiege().checkIfCanRegister(player))
				{
					fort.getSiege().registerAttacker(player, true);
				}
			}
			else if (command.equalsIgnoreCase("clear_fortsiege_list"))
			{
				fort.getSiege().clearSiegeClan();
			}
			else if (command.equalsIgnoreCase("endfortsiege"))
			{
				fort.getSiege().endSiege();
			}
			else if (command.equalsIgnoreCase("setfort"))
			{
				if (player == null || player.getClan() == null)
				{
					admin.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				}
				else if (player.getClan().getHasFort() > 0)
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "The clan already has a Fort");
				}
				else
				{
					fort.setOwner(player.getClan(), false);
				}
			}
			else if (command.equalsIgnoreCase("removefort"))
			{
				L2Clan clan = fort.getOwnerClan();
				if (clan != null)
				{
					fort.removeOwner(true);
				}
				else
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Cannot delete owner");
				}
			}
			else if (command.equalsIgnoreCase("spawn_fortdoors"))
			{
				fort.resetDoors();
			}
			else if (command.equalsIgnoreCase("startfortsiege"))
			{
				fort.getSiege().startSiege();
			}

			showFortSiegePage(admin, fort);
		}
	}
}