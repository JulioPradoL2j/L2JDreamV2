package com.dream.game.handler.admin;

import com.dream.game.access.gmController;
import com.dream.game.access.gmHandler;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.NpcHtmlMessage;

public class AdminAdmin extends gmHandler
{
	private static final String[] commands =
	{
		"admin",
		"players",
		"effects",
		"gamemenu",
		"gmmenu",
		"bar",
		"mod",
		"listall"
	};

	private static void sendHtml(L2PcInstance admin, String patch)
	{
		String name = patch + ".htm";
		NpcHtmlMessage html = new NpcHtmlMessage(admin.getObjectId());
		html.setFile("data/html/admin/menus/" + name);
		admin.sendPacket(html);
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
		if (command.equals("admin"))
		{
			sendHtml(admin, "main");
		}
		else if (command.equals("players"))
		{
			sendHtml(admin, "players");
		}
		else if (command.equals("effects"))
		{
			sendHtml(admin, "effects");
		}
		else if (command.equals("gamemenu"))
		{
			sendHtml(admin, "game");
		}
		else if (command.equals("mod"))
		{
			sendHtml(admin, "mod");
		}
		else if (command.equals("gmmenu"))
		{
			sendHtml(admin, "submenus/gmmenu");
		}
		else if (command.equals("listall"))
		{
			gmController.getInstance().showCommands(admin);
		}

	}
}