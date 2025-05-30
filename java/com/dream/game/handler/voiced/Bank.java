package com.dream.game.handler.voiced;

import com.dream.Config;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.handler.IVoicedCommandHandler;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.templates.item.L2Item;

public class Bank implements IVoicedCommandHandler
{
	private static String[] _voicedCommands =
	{
		"withdraw",
		"deposit",
		"bank"
	};

	private static void showHelp(L2PcInstance cha)
	{
		L2Item item = ItemTable.getInstance().getTemplate(Config.BANKING_GOLDBAR_ID);
		cha.sendMessage("Exchange: on " + item.getName() + " and back");
		cha.sendMessage(".deposit - for the exchange of " + Config.BANKING_GOLDBAR_PRICE + " on " + item.getName());
		cha.sendMessage(".withdraw - for the exchange of " + item.getName() + " on " + Config.BANKING_GOLDBAR_PRICE + " Adena ");
		cha.sendMessage("Make sure you have enough items for the transaction");
	}

	@Override
	public String getDescription(String command)
	{
		L2Item item = ItemTable.getInstance().getTemplate(Config.BANKING_GOLDBAR_ID);
		return "Exchange: on " + item.getName() + " and back.";
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}

	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (!Config.BANKING_ENABLED)
			return false;
		else if (command.startsWith("deposit"))
		{
			if (activeChar.getAdena() >= Config.BANKING_GOLDBAR_PRICE)
			{
				activeChar.reduceAdena("banking", Config.BANKING_GOLDBAR_PRICE, null, true);
				activeChar.addItem("banking", Config.BANKING_GOLDBAR_ID, 1, null, true);
			}
			else
			{
				showHelp(activeChar);
			}
		}
		else if (command.startsWith("withdraw"))
		{
			L2ItemInstance item = activeChar.getInventory().getItemByItemId(Config.BANKING_GOLDBAR_ID);
			if (item != null && activeChar.getAdena() + Config.BANKING_GOLDBAR_PRICE < Integer.MAX_VALUE)
			{
				activeChar.destroyItemByItemId("banking", Config.BANKING_GOLDBAR_ID, 1, null, true);
				activeChar.addAdena("banking", Config.BANKING_GOLDBAR_PRICE, null, true);
			}
			else
			{
				showHelp(activeChar);
			}
		}
		else if (command.startsWith("bank"))
		{
			showHelp(activeChar);

		}

		else
		{
			showHelp(activeChar);
		}
		return false;
	}
}