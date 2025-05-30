package com.dream.game.model.actor.instance;

import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

public class L2FortSupportUnitInstance extends L2NpcInstance
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;

	private final int[] TalismanIds =
	{
		9914,
		9915,
		9917,
		9918,
		9919,
		9920,
		9921,
		9922,
		9923,
		9924,
		9926,
		9927,
		9928,
		9930,
		9931,
		9932,
		9933,
		9934,
		9935,
		9936,
		9937,
		9938,
		9939,
		9940,
		9941,
		9942,
		9943,
		9944,
		9945,
		9946,
		9947,
		9948,
		9949,
		9950,
		9951,
		9952,
		9953,
		9954,
		9955,
		9956,
		9957,
		9958,
		9959,
		9960,
		9961,
		9962,
		9963,
		9964,
		9965,
		9966,
		10141,
		10142,
		10158
	};

	public L2FortSupportUnitInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("Chat"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch (IndexOutOfBoundsException ioobe)
			{
			}
			catch (NumberFormatException nfe)
			{
			}
			showChatWindow(player, val);
		}
		else if (command.startsWith("ExchangeKE"))
		{
			int item = TalismanIds[Rnd.get(TalismanIds.length)];

			if (player.destroyItemByItemId("FortSupportUnit", 9912, 10, this, false))
			{
				player.addItem("FortSupportUnit", item, 1, player, true, true);
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(9912).addNumber(10));
			}
			else
			{
				String filename = "data/html/fortress/supportunit-no-KE.htm";
				showChatWindow(player, filename);
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/fortress/supportunit-no.htm";

		int condition = validateCondition(player);
		if (condition > COND_ALL_FALSE)
			if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			{
				filename = "data/html/fortress/supportunit-busy.htm";
			}
			else if (condition == COND_OWNER)
				if (val == 0)
				{
					filename = "data/html/fortress/supportunit.htm";
				}
				else
				{
					filename = "data/html/fortress/supportunit-" + val + ".htm";
				}
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", String.valueOf(getName() + " " + getTitle()));
		player.sendPacket(html);
	}

	protected int validateCondition(L2PcInstance player)
	{
		if (getFort() != null && getFort().getFortId() > 0)
			if (player.getClan() != null)
				if (getFort().getSiege().getIsInProgress())
					return COND_BUSY_BECAUSE_OF_SIEGE;
				else if (getFort().getOwnerClan() == player.getClan())
					return COND_OWNER;
		return COND_ALL_FALSE;
	}
}