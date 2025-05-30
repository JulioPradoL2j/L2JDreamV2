package com.dream.game.handler.admin;

import com.dream.Config;
import com.dream.game.L2GameServer;
import com.dream.game.Shutdown;
import com.dream.game.access.gmHandler;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.AuthServerThread;
import com.dream.game.network.Disconnection;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.gameserverpackets.ServerStatus;
import com.dream.game.network.serverpackets.NpcHtmlMessage;

public class AdminServer extends gmHandler
{
	private static final String[] commands =
	{
		"restart",
		"shutdown",
		"abort",
		"server",
		"cleanup",
		"kickall",
		"onlygm",
		"forall",
		"maxplayer"
	};

	private static void serverShutdown(int seconds, boolean restart)
	{
		Shutdown.getInstance().startShutdown("GM", seconds, restart ? Shutdown.ShutdownModeType.RESTART : Shutdown.ShutdownModeType.SHUTDOWN);
	}

	private static void showPage(L2PcInstance player)
	{
		long freeMem = (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()) / 1048576;
		long totalMem = Runtime.getRuntime().maxMemory() / 1048576;
		String mem = "Free: " + freeMem + " Mb. Total: " + totalMem + " Mb.";
		long sUptime, sHour, sMinutes, sSeconds = 0;
		String sTime = "";
		sUptime = (System.currentTimeMillis() - L2GameServer._upTime) / 1000;
		sHour = sUptime / 3600;
		sMinutes = (sUptime - sHour * 3600) / 60;
		sSeconds = sUptime - sHour * 3600 - sMinutes * 60;
		sTime = sHour + " h " + sMinutes + " min " + sSeconds + " sec.";

		NpcHtmlMessage html = new NpcHtmlMessage(player.getObjectId());
		html.setFile("data/html/admin/menus/server.htm");
		html.replace("%meminfo%", mem);
		html.replace("%os%", System.getProperty("os.name"));
		html.replace("%time%", sTime);
		html.replace("%online%", L2World.getInstance().getAllPlayersCount());
		html.replace("%max%", Config.MAXIMUM_ONLINE_USERS);
		html.replace("%geo%", Config.GEODATA ? "Loaded" : "Is Off");
		player.sendPacket(html);
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

		String command = params[0];
		if (command.equals("server"))
		{
			showPage(admin);
			return;
		}
		else if (command.equals("maxplayer"))
		{
			if (params.length < 2)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Set the number of players");
				showPage(admin);
				return;
			}

			try
			{
				AuthServerThread.getInstance().setMaxPlayer(Integer.parseInt(params[1]));
				Config.MAXIMUM_ONLINE_USERS = Integer.parseInt(params[1]);
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Maximum online, set " + Config.MAXIMUM_ONLINE_USERS);
			}
			catch (Exception e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Set the number of players");
			}
			showPage(admin);
			return;
		}
		else if (command.equals("kickall"))
		{
			int count = 0;
			for (L2PcInstance pl : L2World.getInstance().getAllPlayers())
				if (pl != null && !pl.isGM())
				{
					if (pl.isOfflineTrade())
					{
						pl.setOfflineTrade(false);
						pl.standUp();
					}
					new Disconnection(pl).defaultSequence(false);
					count++;
				}
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Removed from the game " + count + " players");
			showPage(admin);
			return;
		}
		else if (command.equals("onlygm"))
		{
			AuthServerThread.getInstance().setServerStatus(ServerStatus.STATUS_GM_ONLY);
			Config.SERVER_GMONLY = true;
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Log on to the server only for the Gm Included.");
			showPage(admin);
			return;
		}
		else if (command.equals("forall"))
		{
			AuthServerThread.getInstance().setServerStatus(ServerStatus.STATUS_AUTO);
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Log on to the server only for the off Gm.");
			Config.SERVER_GMONLY = false;
			showPage(admin);
			return;
		}
		else if (command.equals("restart"))
		{
			if (params.length < 2)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Set the time before restart (in MS)");
				showPage(admin);
				return;
			}

			try
			{
				serverShutdown(Integer.parseInt(params[1]), true);
			}
			catch (Exception e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Set the time before restart (in seconds)");
			}
			showPage(admin);
			return;
		}
		else if (command.equals("shutdown"))
		{
			if (params.length < 2)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Set sleep time (in seconds)");
				showPage(admin);
				return;
			}

			try
			{
				serverShutdown(Integer.parseInt(params[1]), false);
			}
			catch (Exception e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Set sleep time (in seconds)");
			}
			showPage(admin);
			return;
		}
		else if (command.equals("abort"))
		{
			Shutdown.getInstance().abort();
			showPage(admin);
			return;
		}
		else if (command.equals("cleanup"))
		{
			System.gc();
			System.runFinalization();
			showPage(admin);
			return;
		}
	}
}