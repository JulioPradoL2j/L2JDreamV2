package com.dream.game.handler.admin;

import com.dream.Config;
import com.dream.game.access.gmCache;
import com.dream.game.access.gmHandler;
import com.dream.game.cache.HtmCache;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.sql.SpawnTable;
import com.dream.game.datatables.xml.BuyListTable;
import com.dream.game.datatables.xml.DoorTable;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.datatables.xml.TeleportLocationTable;
import com.dream.game.datatables.xml.ZoneTable;
import com.dream.game.manager.QuestManager;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.multisell.L2Multisell;
import com.dream.game.network.SystemChatChannelId;

public class AdminReload extends gmHandler
{
	private static final String[] commands =
	{
		"reload",
		"reload_menu",
		"config_reload",
		"config"
	};

	private static void sendConfigReloadPage(L2PcInstance activeChar)
	{
		AdminMethods.showMenuPage(activeChar, "config.htm");
		activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "WARNING: There are several known issues regarding this feature. Reloading server data during runtime is STRONGLY NOT RECOMMENDED for live servers, just for developing environments.");
	}

	private static void sendReloadPage(L2PcInstance activeChar)
	{
		AdminMethods.showSubMenuPage(activeChar, "reload_menu.htm");
		activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "WARNING: There are several known issues regarding this feature. Reloading server data during runtime is STRONGLY NOT RECOMMENDED for live servers, just for developing environments.");
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

		if (command.equals("config"))
		{
			sendConfigReloadPage(admin);
			return;
		}
		else if (command.equals("config_reload"))
		{
			String type = "";
			if (params.length > 1)
			{
				type = params[1];
			}
			else
			{
				sendConfigReloadPage(admin);
				return;
			}

			try
			{
				if (type.equals("rates"))
				{
					Config.loadRatesConfig();
					sendConfigReloadPage(admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Rates <config> reload complete.");
					return;
				}
				else if (type.equals("fun_events"))
				{
					Config.loadMainEventsConfig();
					sendConfigReloadPage(admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "FunEvents <config> reload complete.");

				}
				else if (type.equals("options"))
				{
					Config.loadOptionsConfig();
					sendConfigReloadPage(admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Options <config> reload complete.");
					return;
				}
				else if (type.equals("altgame"))
				{
					Config.loadAltConfig();
					sendConfigReloadPage(admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Altgame <config> reload complete.");
					return;
				}
				else if (type.equals("gmaccess"))
				{
					Config.loadGmAccess();
					sendConfigReloadPage(admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "GMAccess <config> reload complete.");
					return;
				}
				else if (type.equals("sayfilter"))
				{
					Config.unallocateFilterBuffer();
					Config.loadFilter();
					sendConfigReloadPage(admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "SayFilter <config> reload complete.");
					return;
				}
				else if (type.equals("all"))
				{
					Config.unallocateFilterBuffer();
					Config.loadAll();
					sendConfigReloadPage(admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "All <configs> reload complete.");
					return;
				}
				else if (type.equals("entity"))
				{
					Config.loadSiegeConfig();
					sendConfigReloadPage(admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Entity <config> reload complete.");
					return;
				}
				else if (type.equals("npc"))
				{
					Config.loadNpcConfig();
					sendConfigReloadPage(admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Npc <config> reload complete.");
					return;
				}
				else if (type.equals("custom"))
				{
					Config.loadCustomConfig();
					sendConfigReloadPage(admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Custom <config> reload complete.");
					return;
				}
				else if (type.equals("player"))
				{
					Config.loadPlayerConfig();
					sendConfigReloadPage(admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Player <config> reload complete.");
					return;
				}
				else if (type.equals("olympiad"))
				{
					Config.loadOllyConfig();
					sendConfigReloadPage(admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Olympiad <config> reload complete.");
					return;
				}
				else if (type.equals("mod"))
				{
					Config.loadMods();
					sendConfigReloadPage(admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Mod's <config> reload complete.");
					return;
				}
			}
			catch (Exception e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Use: //reload <type>");
				return;
			}
			return;
		}
		else if (command.equals("reload_menu"))
		{
			sendReloadPage(admin);
		}
		else if (command.startsWith("reload"))
		{
			String type = "";
			if (params.length > 1)
			{
				type = params[1];
			}
			else
			{
				sendReloadPage(admin);
				return;
			}

			try
			{
				if (type.equals("multisell"))
				{
					L2Multisell.getInstance().reload();
					sendReloadPage(admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Multisell Reload Complete.");
					return;
				}
				else if (type.startsWith("door"))
				{
					DoorTable.getInstance().reloadAll();
					sendReloadPage(admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Doors Reload Complete.");
					return;
				}
				else if (type.startsWith("teleport"))
				{
					TeleportLocationTable.getInstance().reloadAll();
					sendReloadPage(admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Teleport Reload Complete.");
					return;
				}
				else if (type.startsWith("skill"))
				{
					SkillTable.reload();
					sendReloadPage(admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Skills reload Complete.");
					return;
				}
				else if (type.equals("npcs"))
				{
					NpcTable.getInstance().cleanUp();
					NpcTable.getInstance().reloadAll();
					sendReloadPage(admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "NPCs Reload Complete.");
					return;
				}
				else if (type.equals("script"))
				{
					QuestManager.getInstance().reloadAllQuests();
					sendReloadPage(admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Script's Reload Complete.");
					return;
				}
				else if (type.startsWith("html"))
				{
					HtmCache.getInstance().reload();
					sendReloadPage(admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "HTML's Reload Complete.");
					return;
				}
				else if (type.startsWith("item"))
				{
					ItemTable.reload();
					sendReloadPage(admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Item's Reload Complete.");
					return;
				}
				else if (type.startsWith("tradelist"))
				{
					BuyListTable.getInstance().reload();
					sendReloadPage(admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Buylist's Reload Complete.");
					return;
				}
				else if (type.startsWith("zone"))
				{
					ZoneTable.getInstance().reload();
					sendReloadPage(admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Zone's Reload Complete.");
					return;
				}
				else if (type.equals("spawnlist"))
				{
					SpawnTable.getInstance().reloadAll();
					sendReloadPage(admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Spawn's Reload Complete.");
					return;
				}
				else if (type.startsWith("gmcache"))
				{
					gmCache.getInstance().loadAccess(true);
					sendReloadPage(admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "GmCache Reload Complete.");
					return;
				}
			}
			catch (Exception e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage:  //reload <type>");
				return;
			}
		}
	}
}