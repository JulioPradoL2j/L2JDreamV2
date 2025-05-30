package com.dream.game.handler.admin;

import com.dream.game.access.gmHandler;
import com.dream.game.datatables.GmListTable;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.idfactory.IdFactory;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2ControllableMobInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2SummonInstance;
import com.dream.game.model.base.Experience;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.PetInfo;
import com.dream.game.taskmanager.DecayTaskManager;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.templates.item.L2Item;

import java.util.Collection;

public class AdminCommands extends gmHandler
{
	private static final String[] commands =
	{
		"gmliston",
		"gmlistoff",
		"silence",
		"diet",
		"tradeoff",
		"summon_npc",
		"summon_item",
		"unsummon",
		"itemcreate",
		"create_item",
		"clearpc",
		"create_adena",
		"summon",
		"ride_wyvern",
		"ride_strider",
		"ride_wolf",
		"unride_wyvern",
		"unride_strider",
		"unride_wolf",
		"unride",
		"res",
		"gmchat",
		"snoop",
		"unsnoop",
		"give_item_target",
		"give_item_to_all",
		"clean_inventory"
	};
	@SuppressWarnings("unused")
	private L2PcInstance player;

	private static void createItem(L2PcInstance admin, int id, int num)
	{
		L2Item template = ItemTable.getInstance().getTemplate(id);
		if (template == null)
			return;
		if (num > 20)
			if (!template.isStackable())
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "That's not stackable. Creation in such numbers is not possible");
				return;
			}
		admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Created On " + num + " " + template.getName() + " (" + id + ") in your inventory");
		admin.getInventory().addItem("admin_create", id, num, admin, admin);
		admin.sendPacket(new ItemList(admin, false));
	}

	@SuppressWarnings("null")
	private static void doResurrect(L2Character targetChar, L2PcInstance activeChar)
	{
		if (!targetChar.isDead())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}

		if (targetChar != null && targetChar instanceof L2PcInstance)
		{
			((L2PcInstance) targetChar).restoreExp(100.0);
		}
		else
		{
			DecayTaskManager.getInstance().cancelDecayTask(targetChar);
		}

		targetChar.doRevive();
	}

	private static void removeAllItems(L2PcInstance admin)
	{
		for (L2ItemInstance item : admin.getInventory().getItems())
			if (item.getLocation() == L2ItemInstance.ItemLocation.INVENTORY)
			{
				admin.getInventory().destroyItem("Destroy", item.getObjectId(), item.getCount(), admin, null);
			}
		admin.sendPacket(new ItemList(admin, false));
	}

	private static void snoop(String name, L2PcInstance admin)
	{
		if (name == null)
			return;

		L2PcInstance player = L2World.getInstance().getPlayer(name);
		if (player == null)
		{
			admin.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
			return;
		}
		if (player == admin)
		{
			admin.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}

		if (player.isGM())
		{
			admin.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}

		player.addSnooper(admin);
		admin.addSnooped(player);
	}

	private static void unSnoop(String command, L2PcInstance admin)
	{
		L2Object target = admin.getTarget();
		if (target == null || !(target instanceof L2PcInstance))
		{
			admin.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}

		L2PcInstance player = (L2PcInstance) target;
		player.removeSnooper(admin);
		admin.removeSnooped(player);
		admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Snoop mode stopped");
	}

	private static void UseGmChat(String text, L2PcInstance activeChar)
	{
		try
		{
			GmListTable.broadcastToGMs(new CreatureSay(activeChar.getObjectId(), SystemChatChannelId.Chat_Alliance, activeChar.getName(), text));
		}
		catch (StringIndexOutOfBoundsException e)
		{
		}
	}

	public void adminSummon(L2PcInstance admin, int npcId)
	{
		if (admin.getPet() != null)
		{
			admin.sendPacket(SystemMessageId.S2_S1);
			admin.getPet().unSummon(admin);
		}

		L2NpcTemplate summonTemplate = NpcTable.getInstance().getTemplate(npcId);
		L2SummonInstance summon = new L2SummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, admin, null);

		summon.setTitle(admin.getName());
		summon.setExpPenalty(0);
		if (summon.getLevel() >= Experience.LEVEL.length)
		{
			summon.getStat().setExp(Experience.LEVEL[Experience.LEVEL.length - 1]);
		}
		else
		{
			summon.getStat().setExp(Experience.LEVEL[summon.getLevel() % Experience.LEVEL.length]);
		}

		summon.getStat().setExp(0);
		summon.getStatus().setCurrentHp(summon.getMaxHp());
		summon.getStatus().setCurrentMp(summon.getMaxMp());
		summon.setHeading(admin.getHeading());
		summon.setRunning();
		admin.setPet(summon);
		L2World.getInstance().storeObject(summon);
		summon.spawnMe(admin.getX() + 50, admin.getY() + 100, admin.getZ());
		summon.setFollowStatus(true);
		summon.setShowSummonAnimation(false);
		admin.sendPacket(new PetInfo(summon, 0));
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

		if (command.equals("gmliston"))
		{
			GmListTable.getInstance().showGm(admin);
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You've signed up for gmList");
			return;
		}
		else if (command.equals("gmlistoff"))
		{
			GmListTable.getInstance().hideGm(admin);
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You have cancelled the registration of the gmList");
			return;
		}
		else if (command.equals("silence"))
		{
			if (admin.getMessageRefusal())
			{
				admin.setMessageRefusal(false);
				admin.sendPacket(SystemMessageId.MESSAGE_ACCEPTANCE_MODE);
			}
			else
			{
				admin.setMessageRefusal(true);
				admin.sendPacket(SystemMessageId.MESSAGE_REFUSAL_MODE);
			}
			return;
		}
		else if (command.equals("diet"))
		{
			try
			{
				if (params[1].equalsIgnoreCase("on"))
				{
					admin.setDietMode(true);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Weight limit is disabled");
				}
				else if (params[1].equalsIgnoreCase("off"))
				{
					admin.setDietMode(false);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Weight limit is enabled");
				}
			}
			catch (Exception e)
			{
				if (admin.getDietMode())
				{
					admin.setDietMode(false);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Weight limit is enabled");
				}
				else
				{
					admin.setDietMode(true);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Weight limit is disabled");
				}
			}
			finally
			{
				admin.refreshOverloaded();
			}
			return;
		}
		else if (command.equals("tradeoff"))
		{
			try
			{
				if (params[1].equalsIgnoreCase("on"))
				{
					admin.setTradeRefusal(true);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Trade refuse.");
				}
				else if (params[1].equalsIgnoreCase("off"))
				{
					admin.setTradeRefusal(false);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Trade accept.");
				}
			}
			catch (Exception ex)
			{
				if (admin.getTradeRefusal())
				{
					admin.setTradeRefusal(false);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Trade refuse.");
				}
				else
				{
					admin.setTradeRefusal(true);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Trade accpet.");
				}
			}
			return;
		}
		else if (command.equals("summon_npc"))
		{
			try
			{
				int npcId = Integer.parseInt(params[1]);
				if (npcId != 0)
				{
					adminSummon(admin, npcId);
				}
			}
			catch (Exception e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //summon_npc <npcid>");
			}
			return;
		}
		else if (command.equals("summon_item"))
		{
			try
			{
				int id = Integer.parseInt(params[1]);
				if (admin.addItem("GM", id, 1, admin, true, true) == null)
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "This thing can not be created");
				}
			}
			catch (Exception e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //summon_item <itemid>");
			}
			return;
		}
		else if (command.equals("unsummon"))
			if (admin.getPet() != null)
			{
				admin.getPet().unSummon(admin);
			}
		if (command.equals("itemcreate"))
		{
			AdminMethods.showSubMenuPage(admin, "itemcreation_menu.htm");
		}
		else if (command.startsWith("create_adena"))
		{
			try
			{
				int numval = Integer.parseInt(params[1]);

				L2Object target = admin.getTarget();
				L2PcInstance player = null;

				if (target instanceof L2PcInstance)
				{
					player = (L2PcInstance) target;
				}
				else
				{
					player = admin;
				}

				player.getInventory().addItem("admin_create_adena", 57, numval, admin, admin);
				player.sendMessage(admin.getName() + " added you " + numval + " Adena in stock");
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You have added " + numval + " Adena player " + player.getName());
			}
			catch (Exception e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Cannot create Adena");
			}
		}
		else if (command.startsWith("create_item") || command.startsWith("summon"))
		{
			try
			{
				int idval = Integer.parseInt(params[1]);
				int numval = Integer.parseInt(params[2]);
				L2PcInstance player = admin;
				if (params.length == 4)
					if (admin.getTarget() instanceof L2PcInstance)
					{
						player = (L2PcInstance) admin.getTarget();
					}
				createItem(player, idval, numval);
			}
			catch (Exception e)
			{
			}
			AdminMethods.showSubMenuPage(admin, "itemcreation_menu.htm");
		}
		else if (command.startsWith("give_item_target"))
		{
			try
			{
				L2PcInstance target;
				if (admin.getTarget() instanceof L2PcInstance)
					target = (L2PcInstance) admin.getTarget();
				else
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Invalid target.");
					return;
				}

				int idval = Integer.parseInt(params[1]);
				int numval = Integer.parseInt(params[2]);
				player = admin;
				if (params.length == 4)
					if (admin.getTarget() instanceof L2PcInstance)
					{
						player = (L2PcInstance) admin.getTarget();
					}
				createItem(target, idval, numval);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //give_item_target <itemId> [amount]");
			}
			catch (NumberFormatException nfe)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Specify a valid number.");
			}
			AdminMethods.showSubMenuPage(admin, "itemcreation_menu.htm");
		}
		else if (command.startsWith("give_item_to_all"))
		{
			int idval = Integer.parseInt(params[1]);
			int numval = Integer.parseInt(params[2]);
			player = admin;
			if (params.length == 4)
				if (admin.getTarget() instanceof L2PcInstance)
				{
					player = (L2PcInstance) admin.getTarget();
				}
			int counter = 0;
			L2Item template = ItemTable.getInstance().getTemplate(idval);
			if (template == null)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "This item doesn't exist.");
				return;
			}
			if (numval > 10 && !template.isStackable())
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "This item does not stack - Creation aborted.");
				return;
			}
			Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers();
			{
				for (L2PcInstance onlinePlayer : pls)
				{
					if (admin != onlinePlayer && onlinePlayer.isOnline() == 1 && (onlinePlayer.getClient() != null))
					{
						onlinePlayer.getInventory().addItem("Admin", idval, numval, onlinePlayer, admin);
						onlinePlayer.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Admin spawned " + numval + " " + template.getName() + " in your inventory.");
						onlinePlayer.sendPacket(new ItemList(onlinePlayer, false));
						counter++;
					}
				}
			}
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", counter + " players rewarded with " + template.getName());
		}
		else if (command.startsWith("clean_inventory"))
		{
			L2PcInstance target;
			if (admin.getTarget() instanceof L2PcInstance)
			{
				target = (L2PcInstance) admin.getTarget();
				cleanInventory(admin, target);
			}
			else
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Invalid target.");
				return;
			}
			AdminMethods.showSubMenuPage(admin, "itemcreation_menu.htm");
		}
		else if (command.equals("clearpc"))
		{
			removeAllItems(admin);
		}
		else if (command.startsWith("ride"))
		{
			int _petRideId = 0;

			if (admin.isMounted() || admin.getPet() != null)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You already have summon.");
				return;
			}
			if (command.equals("ride_wyvern"))
			{
				_petRideId = 12621;
			}
			else if (command.equals("ride_strider"))
			{
				_petRideId = 12526;
			}
			else if (command.equals("ride_wolf"))
			{
				_petRideId = 16041;
			}
			else
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Command '" + command + "' not recognized");
				return;
			}
			admin.mount(_petRideId, 0, false);
			return;
		}
		else if (command.startsWith("unride"))
		{
			admin.dismount();
			return;
		}
		else if (command.equals("res"))
		{
			if (params.length > 1)
			{
				try
				{
					int radius = Integer.parseInt(params[1]);

					for (L2Character knownChar : admin.getKnownList().getKnownCharactersInRadius(radius))
					{
						if (knownChar == null || knownChar instanceof L2ControllableMobInstance)
						{
							continue;
						}
						doResurrect(knownChar, admin);
					}
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "The resurrection complete.");
				}
				catch (Exception e)
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //res [radius]");
					return;
				}
			}
			else
			{
				L2Object obj = admin.getTarget();
				if (obj == null)
				{
					obj = admin;
				}

				if (obj instanceof L2Character && !(obj instanceof L2ControllableMobInstance))
				{
					doResurrect((L2Character) obj, admin);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "The resurrection complete.");
				}
				else
				{
					admin.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
			}
		}
		else if (command.startsWith("gmchat"))
		{
			try
			{
				String text = "";
				for (int x = 1; x < params.length; x++)
				{
					text += " " + params[x];
				}

				UseGmChat(text, admin);
			}
			catch (Exception e)
			{
			}
			AdminMethods.showSubMenuPage(admin, "gmmenu.htm");
			return;
		}
		else if (command.startsWith("snoop"))
		{
			if (params.length > 1)
			{
				snoop(params[1], admin);
			}
			else
			{
				admin.sendPacket(SystemMessageId.INCORRECT_TARGET);
			}
			return;
		}
		else if (command.startsWith("unsnoop"))
		{
			if (params.length > 1)
			{
				unSnoop(params[1], admin);
			}
			else
			{
				admin.sendPacket(SystemMessageId.INCORRECT_TARGET);
			}
			return;
		}
	}

	private static void cleanInventory(L2PcInstance activeChar, L2PcInstance target)
	{
		for (L2ItemInstance item : target.getInventory().getItems())
		{
			if (item.getLocation() == L2ItemInstance.ItemLocation.INVENTORY)
			{
				target.getInventory().destroyItem("Destroy", item.getObjectId(), item.getCount(), activeChar, null);
			}
		}

		target.sendPacket(new ItemList(target, false));

		if (activeChar != target)
			target.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Admin cleaned your Inventory!");
		activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You have cleaned " + target.getName() + " inventory!");
	}
}