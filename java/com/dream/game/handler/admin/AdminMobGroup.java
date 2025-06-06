package com.dream.game.handler.admin;

import com.dream.game.access.gmHandler;
import com.dream.game.datatables.MobGroupTable;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.model.MobGroup;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.network.serverpackets.SetupGauge;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.util.Broadcast;

public class AdminMobGroup extends gmHandler
{
	private static final String[] commands =
	{
		"mobmenu",
		"mobgroup_list",
		"mobgroup_create",
		"mobgroup_remove",
		"mobgroup_delete",
		"mobgroup_spawn",
		"mobgroup_unspawn",
		"mobgroup_kill",
		"mobgroup_idle",
		"mobgroup_attack",
		"mobgroup_rnd",
		"mobgroup_return",
		"mobgroup_follow",
		"mobgroup_casting",
		"mobgroup_nomove",
		"mobgroup_attackgrp",
		"mobgroup_invul"
	};

	private static void attack(String command, L2PcInstance admin, L2Character target)
	{
		int groupId;
		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (Exception e)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Incorrect command arguments.");
			return;
		}
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Invalid group specified.");
			return;
		}
		group.setAttackTarget(target);
	}

	private static void attackGrp(String command, L2PcInstance admin)
	{
		int groupId;
		int othGroupId;

		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
			othGroupId = Integer.parseInt(command.split(" ")[2]);
		}
		catch (Exception e)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //mobgroup_attackgrp <groupId> <TargetGroupId>");
			return;
		}
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Invalid group specified.");
			return;
		}
		MobGroup othGroup = MobGroupTable.getInstance().getGroup(othGroupId);
		if (othGroup == null)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Incorrect target group.");
			return;
		}

		group.setAttackGroup(othGroup);
	}

	private static void createGroup(String command, L2PcInstance admin)
	{
		int groupId;
		int templateId;
		int mobCount;

		try
		{
			String[] cmdParams = command.split(" ");

			groupId = Integer.parseInt(cmdParams[1]);
			templateId = Integer.parseInt(cmdParams[2]);
			mobCount = Integer.parseInt(cmdParams[3]);
		}
		catch (Exception e)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //mobgroup_create <group> <npcid> <count>");
			return;
		}

		if (MobGroupTable.getInstance().getGroup(groupId) != null)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Mob group " + groupId + " already exists.");
			return;
		}

		L2NpcTemplate template = NpcTable.getInstance().getTemplate(templateId);

		if (template == null)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Invalid NPC ID specified.");
			return;
		}

		MobGroup group = new MobGroup(groupId, template, mobCount);
		MobGroupTable.getInstance().addGroup(groupId, group);

		admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Mob group " + groupId + " created.");
	}

	private static void doAnimation(L2PcInstance admin)
	{
		Broadcast.toSelfAndKnownPlayersInRadius(admin, new MagicSkillUse(admin, admin, 1008, 1, 4000, 0, false), 2250000);
		admin.sendPacket(new SetupGauge(0, 4000));
	}

	private static void follow(String command, L2PcInstance admin, L2Character target)
	{
		int groupId;
		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (Exception e)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Incorrect command arguments.");
			return;
		}
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Invalid group specified.");
			return;
		}
		group.setFollowMode(target);
	}

	private static void idle(String command, L2PcInstance admin)
	{
		int groupId;
		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (Exception e)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Incorrect command arguments.");
			return;
		}
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Invalid group specified.");
			return;
		}
		group.setIdleMode();
	}

	private static void invul(String command, L2PcInstance admin)
	{
		int groupId;
		String enabled;
		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
			enabled = command.split(" ")[2];
		}
		catch (Exception e)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //mobgroup_invul <groupId> <on|off>");
			return;
		}
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Invalid group specified.");
			return;
		}

		if (enabled.equalsIgnoreCase("on") || enabled.equalsIgnoreCase("true"))
		{
			group.setInvul(true);
		}
		else if (enabled.equalsIgnoreCase("off") || enabled.equalsIgnoreCase("false"))
		{
			group.setInvul(false);
		}
		else
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Incorrect command arguments.");
		}
	}

	private static void killGroup(String command, L2PcInstance admin)
	{
		int groupId;
		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (Exception e)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //mobgroup_kill <groupId>");
			return;
		}
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Invalid group specified.");
			return;
		}
		doAnimation(admin);
		group.killGroup(admin);
	}

	private static void noMove(String command, L2PcInstance admin)
	{
		int groupId;
		String enabled;

		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
			enabled = command.split(" ")[2];
		}
		catch (Exception e)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //mobgroup_nomove <groupId> <on|off>");
			return;
		}
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Invalid group specified.");
			return;
		}

		if (enabled.equalsIgnoreCase("on") || enabled.equalsIgnoreCase("true"))
		{
			group.setNoMoveMode(true);
		}
		else if (enabled.equalsIgnoreCase("off") || enabled.equalsIgnoreCase("false"))
		{
			group.setNoMoveMode(false);
		}
		else
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Incorrect command arguments.");
		}
	}

	private static void removeGroup(String command, L2PcInstance admin)
	{
		int groupId;
		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (Exception e)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //mobgroup_remove <groupId>");
			return;
		}
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Invalid group specified.");
			return;
		}
		doAnimation(admin);
		group.unspawnGroup();

		if (MobGroupTable.getInstance().removeGroup(groupId))
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Mob group " + groupId + " unspawned and removed.");
		}
	}

	private static void returnToChar(String command, L2PcInstance admin)
	{
		int groupId;
		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (Exception e)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Incorrect command arguments.");
			return;
		}
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Invalid group specified.");
			return;
		}
		group.returnGroup(admin);
	}

	private static void setCasting(String command, L2PcInstance admin)
	{
		int groupId;
		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (Exception e)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //mobgroup_casting <groupId>");
			return;
		}
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Invalid group specified.");
			return;
		}
		group.setCastMode();
	}

	private static void setNormal(String command, L2PcInstance admin)
	{
		int groupId;
		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (Exception e)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Incorrect command arguments.");
			return;
		}
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Invalid group specified.");
			return;
		}
		group.setAttackRandom();
	}

	private static void showGroupList(L2PcInstance admin)
	{
		MobGroup[] mobGroupList = MobGroupTable.getInstance().getGroups();

		admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "======= <Mob Groups> =======");
		for (MobGroup mobGroup : mobGroupList)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", mobGroup.getGroupId() + ": " + mobGroup.getActiveMobCount() + " alive out of " + mobGroup.getMaxMobCount() + " of NPC ID " + mobGroup.getTemplate().getNpcId() + " (" + mobGroup.getStatus() + ")");
		}
		admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "======= <Mob Groups> =======");
	}

	private static void showMainPage(L2PcInstance admin, String command)
	{
		if (command.contains("mobinst"))
		{
			AdminMethods.showHelpPage(admin, "help/mobgrouphelp.htm");
		}
		else
		{
			AdminMethods.showSubMenuPage(admin, "mobgroup_menu.htm");
		}
	}

	private static void spawnGroup(String command, L2PcInstance admin)
	{
		int groupId;
		boolean topos = false;
		int posx = 0;
		int posy = 0;
		int posz = 0;

		try
		{
			String[] cmdParams = command.split(" ");
			groupId = Integer.parseInt(cmdParams[1]);

			try
			{
				posx = Integer.parseInt(cmdParams[2]);
				posy = Integer.parseInt(cmdParams[3]);
				posz = Integer.parseInt(cmdParams[4]);
				topos = true;
			}
			catch (Exception e)
			{

			}
		}
		catch (Exception e)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //mobgroup_spawn <group> [ x y z ]");
			return;
		}
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Invalid group specified.");
			return;
		}
		doAnimation(admin);

		if (topos)
		{
			group.spawnGroup(posx, posy, posz);
		}
		else
		{
			group.spawnGroup(admin);
		}

		admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Mob group " + groupId + " spawned.");
	}

	private static void teleportGroup(String command, L2PcInstance admin)
	{
		int groupId;
		String targetPlayerStr = null;
		L2PcInstance targetPlayer = null;
		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
			targetPlayerStr = command.split(" ")[2];

			if (targetPlayerStr != null)
			{
				targetPlayer = L2World.getInstance().getPlayer(targetPlayerStr);
			}
			if (targetPlayer == null)
			{
				targetPlayer = admin;
			}
		}
		catch (Exception e)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //mobgroup_teleport <groupId> [playerName]");
			return;
		}

		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Invalid group specified.");
			return;
		}
		group.teleportGroup(admin);
	}

	private static void unspawnGroup(String command, L2PcInstance admin)
	{
		int groupId;
		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (Exception e)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //mobgroup_unspawn <groupId>");
			return;
		}
		MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null)
		{
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Invalid group specified.");
			return;
		}
		doAnimation(admin);
		group.unspawnGroup();

		admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Mob group " + groupId + " unspawned.");
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
		for (int x = 1; x < params.length; x++)
		{
			command += " " + params[x];
		}

		if (command.equals("mobmenu"))
		{
			showMainPage(admin, command);
			return;
		}
		else if (command.equals("mobgroup_list"))
		{
			showGroupList(admin);
		}
		else if (command.startsWith("mobgroup_create"))
		{
			createGroup(command, admin);
		}
		else if (command.startsWith("mobgroup_delete") || command.startsWith("mobgroup_remove"))
		{
			removeGroup(command, admin);
		}
		else if (command.startsWith("mobgroup_spawn"))
		{
			spawnGroup(command, admin);
		}
		else if (command.startsWith("mobgroup_unspawn"))
		{
			unspawnGroup(command, admin);
		}
		else if (command.startsWith("mobgroup_kill"))
		{
			killGroup(command, admin);
		}
		else if (command.startsWith("mobgroup_attackgrp"))
		{
			attackGrp(command, admin);
		}
		else if (command.startsWith("mobgroup_attack"))
		{
			if (admin.getTarget() instanceof L2Character)
			{
				L2Character target = (L2Character) admin.getTarget();
				attack(command, admin, target);
			}
		}
		else if (command.startsWith("mobgroup_rnd"))
		{
			setNormal(command, admin);
		}
		else if (command.startsWith("mobgroup_idle"))
		{
			idle(command, admin);
		}
		else if (command.startsWith("mobgroup_return"))
		{
			returnToChar(command, admin);
		}
		else if (command.startsWith("mobgroup_follow"))
		{
			follow(command, admin, admin);
		}
		else if (command.startsWith("mobgroup_casting"))
		{
			setCasting(command, admin);
		}
		else if (command.startsWith("mobgroup_nomove"))
		{
			noMove(command, admin);
		}
		else if (command.startsWith("mobgroup_invul"))
		{
			invul(command, admin);
		}
		else if (command.startsWith("mobgroup_teleport"))
		{
			teleportGroup(command, admin);
		}

		showMainPage(admin, command);
		return;
	}
}