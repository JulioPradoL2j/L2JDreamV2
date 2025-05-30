package com.dream.game.handler.admin;

import com.dream.game.Announcements;
import com.dream.game.access.gmHandler;
import com.dream.game.datatables.xml.MapRegionTable;
import com.dream.game.manager.TownManager;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.mapregion.L2MapRegion;
import com.dream.game.model.mapregion.L2MapRegionRestart;
import com.dream.game.model.world.Location;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;

public class AdminDeveloper extends gmHandler
{
	private final String[] commands =
	{
		"msg",
		"region_check"
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

		final String cmd = params[0];
		if (cmd.equals("msg"))
		{
			try
			{
				int msgId = Integer.parseInt(params[1]);
				admin.sendPacket(SystemMessageId.getSystemMessageId(msgId));
			}
			catch (Exception e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Use: //msg [message_id]");
			}
			return;
		}
		else if (cmd.equals("region_check"))
		{
			L2MapRegion region = MapRegionTable.getInstance().getRegion(admin);

			if (region != null)
			{
				L2MapRegionRestart restart = MapRegionTable.getInstance().getRestartLocation(region.getRestartId(admin.getRace()));

				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Actual region: " + region.getId());
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Respawn position will be: " + restart.getName() + " (" + restart.getLocName() + ")");

				if (restart.getBannedRace() != null)
				{
					L2MapRegionRestart redirect = MapRegionTable.getInstance().getRestartLocation(restart.getRedirectId());
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Banned race: " + restart.getBannedRace().name());
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Redirect To: " + redirect.getName() + " (" + redirect.getLocName() + ")");
				}

				Location loc;
				loc = MapRegionTable.getInstance().getTeleToLocation(admin, MapRegionTable.TeleportWhereType.Castle);
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "TeleToLocation (Castle): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

				loc = MapRegionTable.getInstance().getTeleToLocation(admin, MapRegionTable.TeleportWhereType.ClanHall);
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "TeleToLocation (ClanHall): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

				loc = MapRegionTable.getInstance().getTeleToLocation(admin, MapRegionTable.TeleportWhereType.SiegeFlag);
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "TeleToLocation (SiegeFlag): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

				loc = MapRegionTable.getInstance().getTeleToLocation(admin, MapRegionTable.TeleportWhereType.Town);
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "TeleToLocation (Town): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

				String nearestTown = TownManager.getInstance().getClosestTownName(admin);
				Announcements.getInstance().announceToAll(admin.getName() + " has tried spawn-announce near " + nearestTown + "!");
			}
		}
	}
}