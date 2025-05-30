package com.dream.game.handler.admin;

import com.dream.Config;
import com.dream.game.access.gmHandler;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.itemcontainer.Inventory;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.InventoryUpdate;

public class AdminEnchant extends gmHandler
{
	private static final String[] commands =
	{
		"seteh",
		"setec",
		"seteg",
		"setel",
		"seteb",
		"setew",
		"setes",
		"setle",
		"setre",
		"setlf",
		"setrf",
		"seten",
		"setun",
		"setba",
		"enchant"
	};

	private static void setEnchant(L2PcInstance activeChar, int ench, int armorType)
	{
		L2Object target = activeChar.getTarget();
		if (target == null)
		{
			target = activeChar;
		}
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

		int curEnchant = 0;
		L2ItemInstance itemInstance = null;

		L2ItemInstance parmorInstance = player.getInventory().getPaperdollItem(armorType);
		if (parmorInstance != null && parmorInstance.getLocationSlot() == armorType)
		{
			itemInstance = parmorInstance;
		}
		else
		{
			parmorInstance = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
			if (parmorInstance != null && parmorInstance.getLocationSlot() == Inventory.PAPERDOLL_LRHAND)
			{
				itemInstance = parmorInstance;
			}
		}

		if (itemInstance != null)
		{
			curEnchant = itemInstance.getEnchantLevel();
			player.getInventory().unEquipItemInSlotAndRecord(armorType);
			itemInstance.setEnchantLevel(ench);
			player.getInventory().equipItemAndRecord(itemInstance);
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(itemInstance);
			player.sendPacket(iu);
			player.broadcastUserInfo();

			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "The Player " + player.getName() + " changed level point stuff " + itemInstance.getItem().getName() + " s " + curEnchant + " on " + ench + ".");
			if (activeChar != player)
			{
				player.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "GM changed the level point stuff " + itemInstance.getItem().getName() + " s " + curEnchant + " on " + ench + ".");
			}
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

		String command = params[0];

		if (command.equals("enchant"))
		{
			showMainPage(admin);
			return;
		}
		int armorType = -1;

		if (command.equals("seteh"))
		{
			armorType = Inventory.PAPERDOLL_HEAD;
		}
		else if (command.equals("setec"))
		{
			armorType = Inventory.PAPERDOLL_CHEST;
		}
		else if (command.equals("seteg"))
		{
			armorType = Inventory.PAPERDOLL_GLOVES;
		}
		else if (command.equals("seteb"))
		{
			armorType = Inventory.PAPERDOLL_FEET;
		}
		else if (command.equals("setel"))
		{
			armorType = Inventory.PAPERDOLL_LEGS;
		}
		else if (command.equals("setew"))
		{
			armorType = Inventory.PAPERDOLL_RHAND;
		}
		else if (command.equals("setes"))
		{
			armorType = Inventory.PAPERDOLL_LHAND;
		}
		else if (command.equals("setle"))
		{
			armorType = Inventory.PAPERDOLL_LEAR;
		}
		else if (command.equals("setre"))
		{
			armorType = Inventory.PAPERDOLL_REAR;
		}
		else if (command.equals("setlf"))
		{
			armorType = Inventory.PAPERDOLL_LFINGER;
		}
		else if (command.equals("setrf"))
		{
			armorType = Inventory.PAPERDOLL_RFINGER;
		}
		else if (command.equals("seten"))
		{
			armorType = Inventory.PAPERDOLL_NECK;
		}
		else if (command.equals("setun"))
		{
			armorType = Inventory.PAPERDOLL_UNDER;
		}

		if (armorType != -1)
		{
			try
			{
				int ench = Integer.parseInt(params[1]);
				if (ench < 0 || ench > Config.GM_MAX_ENCHANT)
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You can't sharpen things above " + Config.GM_MAX_ENCHANT);
				}
				else
				{
					setEnchant(admin, ench, armorType);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Please specify a new enchant value.");
			}
			catch (NumberFormatException e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Please specify a new enchant value.");
			}
			catch (ArrayIndexOutOfBoundsException e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Please specify a new enchant value.");
			}
		}
		showMainPage(admin);
	}

	public void showMainPage(L2PcInstance activeChar)
	{
		AdminMethods.showSubMenuPage(activeChar, "enchant_menu.htm");
	}
}