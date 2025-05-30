package com.dream.game.handler.voiced;

import com.dream.game.handler.IVoicedCommandHandler;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.NpcHtmlMessage;

public class AioMenu implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"aiomenu",
		"getaiogoods"
	};

	private static void showMainPage(L2PcInstance activeChar)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());
		html.setFile("data/html/mods/aio-menu.htm");
		html.replace("%player%", activeChar.getName());
		activeChar.sendPacket(html);
	}

	@Override
	public String getDescription(String command)
	{
		if (command.equals("aiomenu"))
			return "Displays a menu of commands.";
		return "In detail in the menu.";
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}

	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		int itemId = 57;
		int itemCount = 3000000;

		L2ItemInstance itemInstance = activeChar.getInventory().getItemByItemId(itemId);

		if (command.startsWith("aiomenu"))
		{
			if (activeChar.isAio())
			{
				showMainPage(activeChar);
				return true;
			}
			return false;
		}
		else if (command.startsWith("getaiogoods"))
		{
			if (!activeChar.isAio())
			{
				return false;
			}
			if (itemInstance == null || !itemInstance.isStackable() && activeChar.getInventory().getInventoryItemCount(itemId, -1) < itemCount)
			{
				activeChar.sendMessage("You do not have enough items!");
				return false;
			}
			if (itemInstance.isStackable())
			{
				if (!activeChar.destroyItemByItemId("Aio Seller", itemId, itemCount, activeChar.getTarget(), true))
				{
					activeChar.sendMessage("You do not have enough items!");
					return false;
				}
			}
			else
				for (int i = 0; i < itemCount; i++)
					activeChar.destroyItemByItemId("Aio Seller", itemId, 1, activeChar.getTarget(), true);

			rewardAio(activeChar);
		}
		return false;
	}

	public void rewardAio(L2PcInstance player)
	{
		player.getInventory().addItem("AIOxGoods", 1458, 200, player, player); // Crystal D-Grade
		player.getInventory().addItem("AIOxGoods", 1459, 200, player, player); // Crystal C-Grade
		player.getInventory().addItem("AIOxGoods", 3952, 1000, player, player); // Blessed Spiritshot: S Grade
		player.getInventory().addItem("AIOxGoods", 8874, 100, player, player); // Einhasad's Holy Water
		player.getInventory().addItem("AIOxGoods", 728, 100, player, player); // Mana Potion
		player.getInventory().addItem("AIOxGoods", 3031, 250, player, player); // Spirit Ore
		player.getInventory().addItem("AIOxGoods", 1785, 250, player, player); // Soul Ore
		player.sendPacket(new ItemList(player, true));
		showMainPage(player);
	}
}