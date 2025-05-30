package com.dream.game.model.entity.events.TvT;

import com.dream.Config;
import com.dream.Message;
import com.dream.game.handler.IVoicedCommandHandler;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.events.GameEvent;
import com.dream.game.network.serverpackets.ActionFailed;

public class VoiceTVTEngine implements IVoicedCommandHandler
{
	@Override
	public String getDescription(String command)
	{
		if (command.equals("tvtjoin"))
			return "Join the TvT tournament.";
		if (command.equals("tvtleave"))
			return "Cancel registration of the TvT.";
		return null;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return new String[]
		{
			"tvtjoin",
			"tvtleave"
		};
	}

	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		int itemId = Config.EVENT_PARTICIPATION_FEE_ID;
		int itemCount = Config.EVENT_PARTICIPATION_FEE_QNT;

		if (activeChar == null)
		{
			return false;
		}

		L2ItemInstance itemInstance = activeChar.getInventory().getItemByItemId(itemId);

		if (command.equals("tvtjoin"))
		{
			if (Config.LIST_EVENT_BLOCKED_CLASSES.contains(activeChar.getTemplate().getClassId().getId()) && !activeChar.isSubClassActive() || activeChar.isAio())
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				activeChar.sendMessage("Dear " + activeChar.getName() + " classes such as " + Config.EVENT_BLOCKED_CLASS_NAMES + " and AIOx cannot participate on events.");
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				if (TvT.getInstance().register(activeChar))
				{
				if (itemInstance == null || !itemInstance.isStackable() && activeChar.getInventory().getInventoryItemCount(itemId, -1) < itemCount)
					{
						activeChar.sendMessage("You do not have enough items, registration failed!");
						TvT.getInstance().remove(activeChar);
						return false;
				}
					if (itemInstance.isStackable())
				{
						if (!activeChar.destroyItemByItemId("TvT Paticipation Fee", itemId, itemCount, activeChar.getTarget(), true))
						{
							activeChar.sendMessage("You do not have enough items, registration failed!");
							TvT.getInstance().remove(activeChar);
							return false;

						}
				}
				else
					for (int i = 0; i < itemCount; i++)
							activeChar.destroyItemByItemId("TvT Paticipation Fee", itemId, 1, activeChar.getTarget(), true);
							activeChar.sendMessage(String.format(Message.getMessage(activeChar, Message.MessageId.MSG_EVENT_REGISTERED), "TvT"));
							return true;
				}
			}
		}
		else if (command.equals("tvtleave"))
		{
			if (TvT.getInstance().getState() == GameEvent.STATE_ACTIVE && TvT.getInstance().isParticipant(activeChar))
			{
				TvT.getInstance().remove(activeChar);
				activeChar.sendMessage(String.format(Message.getMessage(activeChar, Message.MessageId.MSG_EVENT_CANCEL_REG), "TvT"));
			}
			else
			{
				activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_EVENT_NOT_REGISTERED));
			}
			return true;
		}
		return false;
	}
}