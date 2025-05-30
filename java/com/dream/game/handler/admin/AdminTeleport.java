package com.dream.game.handler.admin;

import com.dream.L2DatabaseFactory;
import com.dream.game.access.gmHandler;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.cache.HtmCache;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.sql.SpawnTable;
import com.dream.game.datatables.xml.MapRegionTable.TeleportWhereType;
import com.dream.game.manager.BoatManager;
import com.dream.game.manager.FourSepulchersManager.FourSepulchersMausoleum;
import com.dream.game.manager.RaidBossSpawnManager;
import com.dream.game.manager.grandbosses.BossLair;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Party;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2BoatInstance;
import com.dream.game.model.actor.instance.L2GrandBossInstance;
import com.dream.game.model.actor.instance.L2MinionInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2RaidBossInstance;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.model.world.L2World;
import com.dream.game.model.zone.L2BossZone;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.templates.chars.L2NpcTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.NoSuchElementException;

public class AdminTeleport extends gmHandler
{
	private static final String[] commands =
	{
		"recall_offline",
		"telemode",
		"show_moves",
		"show_moves_other",
		"show_teleport",
		"recall_npc",
		"teleport_to_character",
		"walk",
		"move_to",
		"teleportto",
		"recall",
		"recall_all",
		"tele",
		"go",
		"recallparty",
		"recallclan",
		"recallally",
		"instant_move",
		"sendhome",
		"rblist",
		"startroom"

	};

	
	private static boolean changeCharacterPosition(L2PcInstance activeChar, String name)
	{
		boolean result = false;
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=? WHERE char_name=?");
			statement.setInt(1, activeChar.getX());
			statement.setString(2, name);
			statement.execute();
			statement = con.prepareStatement("UPDATE characters SET y=? WHERE char_name=?");
			statement.setInt(1, activeChar.getY());
			statement.setString(2, name);
			statement.execute();
			statement = con.prepareStatement("UPDATE characters SET z=? WHERE char_name=?");
			statement.setInt(1, activeChar.getZ());
			statement.setString(2, name);
			if (statement.execute())
			{
				result = true;
			}
			statement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return result;
	}

	private static void handleSendhome(L2PcInstance activeChar, String player)
	{
		L2Object obj = null;

		if (player == null)
		{
			obj = activeChar;
		}
		else
		{
			obj = L2World.getInstance().getPlayer(player);
		}

		if (obj != null && obj instanceof L2PcInstance)
		{
			((L2PcInstance) obj).teleToLocation(TeleportWhereType.Town);
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
		}
	}

	private static void recallNPC(L2PcInstance activeChar)
	{
		L2Object obj = activeChar.getTarget();
		if (obj instanceof L2Npc && !(obj instanceof L2MinionInstance) && !(obj instanceof L2RaidBossInstance) && !(obj instanceof L2GrandBossInstance))
		{
			L2Npc target = (L2Npc) obj;

			int monsterTemplate = target.getTemplate().getNpcId();
			L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(monsterTemplate);
			if (template1 == null)
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Wrong Monster.");
				return;
			}

			L2Spawn spawn = target.getSpawn();

			if (spawn == null)
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Incorrect coordinates spawn.");
				return;
			}

			target.decayMe();
			spawn.setLocx(activeChar.getX());
			spawn.setLocy(activeChar.getY());
			spawn.setLocz(activeChar.getZ());
			spawn.setHeading(activeChar.getHeading());
			spawn.respawnNpc(target);
			SpawnTable.getInstance().updateSpawn(spawn);
		}
		else if (obj instanceof L2RaidBossInstance)
		{
			L2RaidBossInstance target = (L2RaidBossInstance) obj;
			RaidBossSpawnManager.getInstance().updateSpawn(target.getNpcId(), activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading());
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
		}
	}

	private static void showTeleportCharWindow(L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		StringBuilder replyMSG = new StringBuilder("<html><title>Teleport character</title>");
		replyMSG.append("<body>");
		replyMSG.append("The character you will teleport is " + player.getName() + ".");
		replyMSG.append("<br>");
		replyMSG.append("Coordinate x");
		replyMSG.append("<edit var=\"char_cord_x\" width=110>");
		replyMSG.append("Coordinate y");
		replyMSG.append("<edit var=\"char_cord_y\" width=110>");
		replyMSG.append("Coordinate z");
		replyMSG.append("<edit var=\"char_cord_z\" width=110>");
		replyMSG.append("<button value=\"Teleport\" action=\"bypass -h admin_teleport_character $char_cord_x $char_cord_y $char_cord_z\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\">");
		replyMSG.append("<button value=\"Teleport near you\" action=\"bypass -h admin_teleport_character " + activeChar.getX() + " " + activeChar.getY() + " " + activeChar.getZ() + "\" width=115 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\">");
		replyMSG.append("<center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></center>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private static void showTeleportWindow(L2PcInstance activeChar)
	{
		AdminMethods.showSubMenuPage(activeChar, "move_menu.htm");
	}

	private static void teleportCharacter(L2PcInstance player, int x, int y, int z)
	{
		if (player != null)
		{
			player.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Are you moved by coordinates.");
			player.getAI().setIntention(CtrlIntention.IDLE);
			player.teleToLocation(x, y, z);
		}
	}

	private static void teleportTo(L2PcInstance activeChar, int x, int y, int z)
	{
		if (activeChar != null)
		{
			activeChar.getAI().setIntention(CtrlIntention.IDLE);
			activeChar.teleToLocation(x, y, z, false);
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Are you moved by coordinates: " + x + " " + y + " " + z);
		}
	}

	private static void teleportToBoat(L2PcInstance activeChar, int id)
	{
		try
		{
			L2BoatInstance boat = BoatManager.getInstance().getBoat(id);
			if (boat != null)
			{
				activeChar.getAI().setIntention(CtrlIntention.IDLE);
				activeChar.teleToLocation(boat.getX(), boat.getY(), boat.getZ() + 500, false);
			}
		}
		catch (NoSuchElementException nsee)
		{
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Invalid koordianty.");
		}
	}

	private static void teleportToCharacter(L2PcInstance activeChar, L2PcInstance target)
	{
		if (target == null)
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		if (target.getObjectId() == activeChar.getObjectId())
		{
			target.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
		}
		else
		{
			activeChar.getAI().setIntention(CtrlIntention.IDLE);
			activeChar.teleToLocation(target.getX(), target.getY(), target.getZ());
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You moved to the character " + target.getName() + ".");
		}
	}

	@Override
	public String[] getCommandList()
	{
		return commands;
	}

	@Override
	public void runCommand(L2PcInstance admin, String... params)
	{
		if (admin == null || params == null)
			return;

		String command = params[0];

		if (command.equals("rblist"))
		{
			String htm = HtmCache.getInstance().getHtm("data/html/admin/tele/raid/header.htm");
			if (params[1].equals("grand"))
			{
				for (BossLair lair : BossLair.getLairs())
					if (lair._bossSpawn != null)
					{
						int x = lair._bossSpawn.getLocx(), y = lair._bossSpawn.getLocy(), z = lair._bossSpawn.getLocz();
						if (lair._bossSpawn.getLastSpawn() != null)
						{
							x = lair._bossSpawn.getLastSpawn().getX();
							y = lair._bossSpawn.getLastSpawn().getY();
							z = lair._bossSpawn.getLastSpawn().getZ();
						}
						htm += "<a action=\"bypass -h admin_move_to " + x + " " + y + " " + z + "\">" + lair._bossSpawn.getTemplate().getName() + " lvl " + lair._bossSpawn.getTemplate().getLevel() + " (" + lair.getState() + ")</a><br1>";
					}

				htm += "</font></body></html>";
				NpcHtmlMessage msg = new NpcHtmlMessage(5);
				msg.setHtml(htm);
				msg.replace("%raidinfo%", "Grand Boss");
				admin.sendPacket(msg);
			}
			else
			{
				int level = Integer.parseInt(params[1]);
				for (L2Spawn spawn : RaidBossSpawnManager.getInstance().getSpawns().values())
					if (spawn.getTemplate().getLevel() >= level && spawn.getTemplate().getLevel() < level + 10)
					{
						htm += "<a action=\"bypass -h admin_move_to " + spawn.getLocx() + " " + spawn.getLocy() + " " + spawn.getLocz() + "\">" + spawn.getTemplate().getName() + " lvl " + spawn.getTemplate().getLevel() + " (" + RaidBossSpawnManager.getInstance().getRaidBossStatusId(spawn.getTemplate().getIdTemplate()) + ")</a><br1>";
					}
				htm += "</font></body></html>";
				NpcHtmlMessage msg = new NpcHtmlMessage(5);
				msg.setHtml(htm);
				msg.replace("%raidinfo%", "Raid Boss " + level + "-" + (level + 9));
				admin.sendPacket(msg);
			}
		}
		else if (command.equals("sendhome"))
		{
			if (params.length > 1)
			{
				handleSendhome(admin, params[1]);
			}
			else
			{
				handleSendhome(admin, null);
			}
		}
		else if (command.equals("recallparty"))
		{
			L2PcInstance player = null;
			if (params.length > 1)
			{
				player = L2World.getInstance().getPlayer(params[1]);
			}
			if (player == null)
			{
				L2Object obj = admin.getTarget();
				if (obj == null)
				{
					obj = admin;
				}

				if (obj instanceof L2PcInstance)
				{
					player = (L2PcInstance) obj;
				}
			}

			if (player != null)
			{
				if (player.getParty() == null)
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "The player is not in the Group");
					return;
				}
				int x = admin.getX(), y = admin.getY(), z = admin.getZ(), count = 0;
				L2Party party = player.getParty();
				if (party == null)
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "The team player " + player.getName() + " not found");
					return;
				}
				for (L2PcInstance pc : party.getPartyMembers())
				{
					if (pc == null || pc.isInJail() || pc.isOfflineTrade())
					{
						continue;
					}

					pc.teleToLocation(x, y, z);
					pc.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Your group should Gm");
					count++;
				}
				if (count > 0)
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "The team player " + player.getName() + " designed for you");
				}
				else
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "The players in the group not found");
				}
			}
			else
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Specify the correct target");
				return;
			}
		}
		else if (command.equals("recallclan"))
		{
			L2PcInstance player = null;
			if (params.length > 1)
			{
				player = L2World.getInstance().getPlayer(params[1]);
			}
			if (player == null)
			{
				L2Object obj = admin.getTarget();
				if (obj == null)
				{
					obj = admin;
				}

				if (obj instanceof L2PcInstance)
				{
					player = (L2PcInstance) obj;
				}
			}

			if (player != null)
			{
				if (player.getClan() == null)
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "No player in the clan");
					return;
				}
				int x = admin.getX(), y = admin.getY(), z = admin.getZ(), count = 0;
				L2Clan clan = player.getClan();
				if (clan == null)
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "The player's clan " + player.getName() + " not found");
					return;
				}
				for (L2PcInstance pc : clan.getOnlineMembersList())
				{
					if (pc == null || pc.isInJail() || pc.isOfflineTrade())
					{
						continue;
					}

					pc.teleToLocation(x, y, z);
					pc.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Your clan is Gm");
					count++;
				}
				if (count > 0)
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "The player's clan " + player.getName() + " is to you");
				}
				else
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Players in the clan are not found");
				}
			}
			else
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Specify the correct target");
				return;
			}
		}
		else if (command.equals("recallally"))
		{
			L2PcInstance player = null;
			if (params.length > 1)
			{
				player = L2World.getInstance().getPlayer(params[1]);
			}
			if (player == null)
			{
				L2Object obj = admin.getTarget();
				if (obj == null)
				{
					obj = admin;
				}

				if (obj instanceof L2PcInstance)
				{
					player = (L2PcInstance) obj;
				}
			}

			if (player != null)
			{
				if (player.getClan() == null || player.getClan().getAllyId() == 0)
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "No player in the Alliance");
					return;
				}
				int x = admin.getX(), y = admin.getY(), z = admin.getZ(), count = 0;
				L2Clan clan = player.getClan();
				if (clan == null)
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Alliance player " + player.getName() + " not found");
					return;
				}
				for (L2PcInstance pc : clan.getOnlineAllyMembers())
				{
					if (pc == null || pc.isInJail() || pc.isOfflineTrade())
					{
						continue;
					}

					pc.teleToLocation(x, y, z);
					pc.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Your Alliance is designed to Gm");
					count++;
				}
				if (count > 0)
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Alliance player " + player.getName() + " is to you");
				}
				else
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Players in the Alliance not found");
				}
			}
			else
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Specify the correct target");
				return;
			}
		}
		else if (command.equals("recall_offline"))
		{
			try
			{
				if (params.length > 1)
				{
					changeCharacterPosition(admin, params[1]);
				}
				else
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //recall_offline [name]");
				}
			}
			catch (Exception e)
			{
			}
			return;
		}
		else if (command.equals("telemode"))
		{
			if (params.length > 1)
			{
				String cmd = params[1];
				if (cmd.equals("demon"))
				{
					admin.setTeleMode(1);
				}
				else if (cmd.equals("norm"))
				{
					admin.setTeleMode(0);
				}
			}
			showTeleportWindow(admin);
			return;
		}
		else if (command.equals("startroom"))
		{
			L2BossZone z = (L2BossZone) admin.getZone("Boss");
			if (z != null && z._lair instanceof FourSepulchersMausoleum)
			{
				((FourSepulchersMausoleum) z._lair).startRoom(Integer.parseInt(params[1]));
				return;
			}
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Not in Four Sepluchers");

		}
		else if (command.equals("show_moves"))
		{
			AdminMethods.showTeleMenuPage(admin, "teleports.htm");
			return;
		}
		else if (command.equals("show_moves_other"))
		{
			AdminMethods.showTeleMenuPage(admin, "areas/areas.html");
			return;
		}
		else if (command.equals("show_teleport"))
		{
			showTeleportCharWindow(admin);
			return;
		}
		else if (command.equals("recall_npc"))
		{
			recallNPC(admin);
			return;
		}
		else if (command.equals("teleport_to_character"))
		{
			if (admin.getTarget() instanceof L2PcInstance)
			{
				teleportToCharacter(admin, (L2PcInstance) admin.getTarget());
			}
			else
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "The goal is not a player.");
			}
			return;
		}
		else if (command.equals("walk"))
		{
			try
			{
				if (params.length >= 4)
				{
					L2CharPosition pos = new L2CharPosition(Integer.parseInt(params[1]), Integer.parseInt(params[2]), Integer.parseInt(params[3]), 0);
					admin.getAI().setIntention(CtrlIntention.MOVE_TO, pos);
				}
				else
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //walk [x] [y] [z]");
				}
			}
			catch (Exception e)
			{
			}
			return;
		}
		else if (command.equals("instant_move"))
		{
			if (admin.getTeleMode() > 0)
			{
				admin.setTeleMode(0);
			}
			else
			{
				admin.setTeleMode(1);
			}

			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "The move mode is changed");
			return;
		}
		else if (command.equals("move_to"))
		{
			try
			{
				if (params.length > 2)
				{
					String val = params[1];
					if (val.equals("boat"))
					{
						teleportToBoat(admin, Integer.parseInt(params[2]));
					}
					else if (params.length >= 4)
					{
						teleportTo(admin, Integer.parseInt(params[1]), Integer.parseInt(params[2]), Integer.parseInt(params[3]));
					}
					else
					{
						admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //move_to [x] [y] [z]");
						AdminMethods.showTeleMenuPage(admin, "teleports.htm");
					}
				}
				else
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //move_to [x] [y] [z]");
					AdminMethods.showTeleMenuPage(admin, "teleports.htm");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //move_to [x] [y] [z]");
				AdminMethods.showTeleMenuPage(admin, "teleports.htm");
			}
			catch (NoSuchElementException nsee)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //move_to [x] [y] [z]");
				AdminMethods.showTeleMenuPage(admin, "teleports.htm");
			}
			catch (NumberFormatException nfe)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //move_to [x] [y] [z]");
				AdminMethods.showTeleMenuPage(admin, "teleports.htm");
			}
			return;
		}
		else if (command.equals("teleportto"))
		{
			try
			{
				if (params.length > 1)
				{
					L2PcInstance player = L2World.getInstance().getPlayer(params[1]);
					if (player != null)
					{
						teleportToCharacter(admin, player);
					}
					else
					{
						admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "The player is not found on the server");
					}
				}
				else
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //teleportto [name]");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //teleportto [name]");
			}
			return;
		}
		else if (command.equals("recall"))
		{
			if (params.length < 1)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //recall [name]");
				return;
			}

			L2PcInstance player = L2World.getInstance().getPlayer(params[1]);
			if (player != null)
			{
				try
				{
					if (player.isInJail())
					{
						admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Player " + player.getName() + " in prison.");
						return;
					}
					teleportCharacter(player, admin.getX(), admin.getY(), admin.getZ());
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Player " + player.getName() + " is to you.");
				}
				catch (StringIndexOutOfBoundsException e)
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //recall [name]");
					return;
				}
				catch (ArrayIndexOutOfBoundsException e)
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //recall [name]");
					return;
				}
			}
			else
			{
				try
				{
					String param = params[1];
					if (changeCharacterPosition(admin, param))
					{
						admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Player " + param + " not in the game. Updated in the database.");
					}
					else
					{
						admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Player " + param + " not in the game. Updated in the database.");
					}
				}
				catch (Exception e)
				{
				}
			}
			return;
		}
		else if (command.equals("recall_all"))
		{
			int count = 0;
			int x = admin.getX();
			int y = admin.getY();
			int z = admin.getZ();
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				if (player == null)
				{
					continue;
				}
				if (player.isInJail())
				{
					continue;
				}
				if (player == admin)
				{
					continue;
				}
				teleportCharacter(player, x, y, z);
				count++;
			}
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Is Intended To " + count + " players.");
			return;
		}
		else if (command.equals("tele"))
		{
			showTeleportWindow(admin);
			return;
		}
		else if (command.startsWith("go"))
		{
			int intVal = 150;
			int x = admin.getX();
			int y = admin.getY();
			int z = admin.getZ();

			try
			{
				if (params.length > 1)
				{
					String dir = params[1];
					intVal = params.length > 2 ? Integer.parseInt(params[2]) : 0;

					if (dir.equals("east"))
					{
						x += intVal;
					}
					else if (dir.equals("west"))
					{
						x -= intVal;
					}
					else if (dir.equals("north"))
					{
						y -= intVal;
					}
					else if (dir.equals("south"))
					{
						y += intVal;
					}
					else if (dir.equals("up"))
					{
						z += intVal;
					}
					else if (dir.equals("down"))
					{
						z -= intVal;
					}
					admin.teleToLocation(x, y, z, false);
					showTeleportWindow(admin);
				}
				else
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //go<north|south|east|west|up|down> [offset]");
				}
			}
			catch (Exception e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //go<north|south|east|west|up|down> [offset]");
			}
			return;
		}
	}
}