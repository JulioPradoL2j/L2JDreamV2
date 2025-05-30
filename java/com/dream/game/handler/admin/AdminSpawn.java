package com.dream.game.handler.admin;

import com.dream.Config;
import com.dream.game.access.gmHandler;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.sql.SpawnTable;
import com.dream.game.manager.DayNightSpawnManager;
import com.dream.game.manager.RaidBossSpawnManager;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2MonsterInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

public class AdminSpawn extends gmHandler
{
	private static final String[] commands =
	{
		"spawn_menu",
		"spawn",
		"cspawn",
		"otspawn",
		"spawn_once",
		"spawnday",
		"spawnnight",
		"returntospawn"

	};

	private static void spawnNpc(L2PcInstance activeChar, int npcId, int count, int radius, boolean saveInDb, boolean respawn, boolean custom)
	{
		L2Object target = activeChar.getTarget();
		if (target == null)
		{
			target = activeChar;
		}

		L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);

		if (template == null)
		{
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "NpcID " + npcId + " not found");
			return;
		}

		if (template.getType().equalsIgnoreCase("L2GrandBoss") && !Config.DEVELOPER)
		{
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Cannot be spawned Grand Bosses");
			return;
		}
		try
		{
			for (int i = 0; i < count; i++)
			{
				int x = target.getX();
				int y = target.getY();
				int z = target.getZ();
				int heading = activeChar.getHeading();

				if (radius > 0 && count > 1)
				{
					int signX = Rnd.nextInt(2) == 0 ? -1 : 1;
					int signY = Rnd.nextInt(2) == 0 ? -1 : 1;
					int randX = Rnd.nextInt(radius);
					int randY = Rnd.nextInt(radius);
					int randH = Rnd.nextInt(0xFFFF);

					x = x + signX * randX;
					y = y + signY * randY;
					heading = randH;
				}

				L2Spawn spawn = new L2Spawn(template);

				if (custom)
				{
					spawn.setCustom();
				}

				spawn.setLocx(x);
				spawn.setLocy(y);
				spawn.setLocz(z);
				spawn.setAmount(1);
				spawn.setHeading(heading);
				spawn.setRespawnDelay(Config.STANDARD_RESPAWN_DELAY);

				if (RaidBossSpawnManager.getInstance().isDefined(spawn.getNpcId()) && respawn && !Config.ALT_DEV_NO_SPAWNS)
				{
					activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You cannot call the " + template.getName() + ". Has already been called");
				}
				else
				{
					if (saveInDb && !Config.ALT_DEV_NO_SPAWNS)
					{
						if (RaidBossSpawnManager.getInstance().getValidTemplate(spawn.getNpcId()) != null)
						{
							spawn.setRespawnMinDelay(43200);
							spawn.setRespawnMaxDelay(129600);
							RaidBossSpawnManager.getInstance().addNewSpawn(spawn, 0, template.getBaseHpMax(), template.getBaseMpMax(), true);
						}
						else
						{
							SpawnTable.getInstance().addNewSpawn(spawn, respawn);
						}
					}
					else
					{
						spawn.spawnOne(true);
					}

					spawn.init();

					if (!respawn)
					{
						spawn.stopRespawn();
					}

					activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Called " + template.getName() + " by coordinates " + target.getX() + " " + target.getY() + " " + target.getZ() + ".");
				}
			}
		}
		catch (Exception e)
		{
		}
	}

	private static void spawnNpc(L2PcInstance activeChar, String npcName, int count, int radius, boolean saveInDb, boolean respawn, boolean custom)
	{
		int npcId = 0;

		for (L2NpcTemplate t : NpcTable.getInstance().getAllTemplates().values())
			if (t.getName().equalsIgnoreCase(npcName.replace("_", " ")))
			{
				npcId = t.getNpcId();
				break;
			}

		if (npcId > 0)
		{
			spawnNpc(activeChar, npcId, count, radius, saveInDb, respawn, custom);
		}
		else
		{
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "NpcID " + npcId + " not found");
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
		if (admin == null)
			return;

		String cmd = params[0];

		if (cmd.equals("spawn_menu"))
		{
			AdminMethods.showSubMenuPage(admin, "spawn_menu.htm");
			return;
		}
		else if (cmd.equals("spawn") || cmd.equals("cspawn") || cmd.equals("otspawn") || cmd.equals("spawn_once"))
		{
			boolean custom = cmd.equals("cspawn");
			boolean respawn = !cmd.equals("spawn_once");
			boolean storeInDb = !cmd.equals("otspawn") && respawn;

			if (params.length >= 2)
			{
				int npcId = 0, count = 1, radius = 300;
				String name = params[1];

				try
				{
					try
					{
						npcId = Integer.parseInt(name);
					}
					catch (NumberFormatException e)
					{
					}
					try
					{
						if (params.length > 2)
						{
							count = Integer.parseInt(params[2]);
							if (params.length > 3)
							{
								radius = Integer.parseInt(params[3]);
							}
						}
					}
					catch (NumberFormatException e)
					{

					}

					if (npcId > 0)
					{
						spawnNpc(admin, npcId, count, radius, storeInDb, respawn, custom);
					}
					else if (name.length() > 0)
					{
						spawnNpc(admin, name, count, radius, storeInDb, respawn, custom);
					}
					else
					{
						AdminMethods.showSubMenuPage(admin, "spawn_menu.htm");
						admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage1: //" + cmd + " [id] [count] [radius]");
						return;
					}
				}
				catch (Exception e)
				{
					AdminMethods.showSubMenuPage(admin, "spawn_menu.htm");
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage2: //" + cmd + " [id] [count] [radius]");
					return;
				}
			}
			else
			{
				AdminMethods.showSubMenuPage(admin, "spawn_menu.htm");
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage3: //" + cmd + " [id] [count] [radius]");
				return;
			}
			return;
		}
		else if (cmd.equals("spawnday"))
		{
			DayNightSpawnManager.getInstance().spawnDayCreatures();
			AdminMethods.showSubMenuPage(admin, "spawn_menu.htm");
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "All day-NPC spawned");
			return;
		}
		else if (cmd.equals("returntospawn"))
		{
			if (admin.getTarget() != null)
				if (admin.getTarget() instanceof L2Npc)
				{
					L2Npc npc = (L2Npc) admin.getTarget();
					if (npc.getSpawn() != null)
					{
						npc.teleToLocation(npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz());
						if (npc instanceof L2MonsterInstance)
						{
							L2MonsterInstance monster = (L2MonsterInstance) npc;
							if (monster.hasMinions())
							{
								monster.callMinions(true);
							}

						}
					}
				}
		}
		else if (cmd.equals("spawnnight"))
		{
			DayNightSpawnManager.getInstance().spawnNightCreatures();
			AdminMethods.showSubMenuPage(admin, "spawn_menu.htm");
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "All night the NPC spawned");
			return;
		}
	}
}