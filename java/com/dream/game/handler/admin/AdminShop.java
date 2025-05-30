package com.dream.game.handler.admin;

import com.dream.game.access.gmHandler;
import com.dream.game.datatables.xml.BuyListTable;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.buylist.NpcBuyList;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.serverpackets.BuyList;

public class AdminShop extends gmHandler
{
	private static final String[] commands =
	{
		"buy",
		"gmshop"
	};

	private static void handleBuyRequest(L2PcInstance activeChar, int id)
	{
		final NpcBuyList list = BuyListTable.getInstance().getBuyList(id);

		if (list != null)
		{
			activeChar.sendPacket(new BuyList(list, activeChar.getAdena(), 0));
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

		if (command.startsWith("buy"))
		{
			try
			{
				handleBuyRequest(admin, Integer.parseInt(params[1]));
			}
			catch (IndexOutOfBoundsException e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Enter the ID store.");
			}
			return;
		}
		else if (command.equals("gmshop"))
		{
			AdminMethods.showSubMenuPage(admin, "adminshop_menu.htm");
			return;
		}
	}
}