package com.dream.game.model.entity.events.CTF;

import com.dream.Config;
import com.dream.Message;
import com.dream.game.handler.IVoicedCommandHandler;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.events.GameEvent;
import com.dream.game.network.serverpackets.ActionFailed;

public class VoiceCTFEngine implements IVoicedCommandHandler
{
    @Override
    public String[] getVoicedCommandList()
    {
        return new String[]
        {
            "ctfjoin",
            "ctfleave"
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

        if (command.equals("ctfjoin"))
		{
			if (Config.LIST_EVENT_BLOCKED_CLASSES.contains(activeChar.getTemplate().getClassId().getId()) && !activeChar.isSubClassActive() || activeChar.isAio())
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				activeChar.sendMessage("Dear " + activeChar.getName() + " classes such as " + Config.EVENT_BLOCKED_CLASS_NAMES + " and AIOx cannot participate on events.");
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				if (CTF.getInstance().register(activeChar))
				{
					if (itemInstance == null || !itemInstance.isStackable() && activeChar.getInventory().getInventoryItemCount(itemId, -1) < itemCount)
					{
						activeChar.sendMessage("You do not have enough items, registration failed!");
						CTF.getInstance().remove(activeChar);
						return false;
				}
					if (itemInstance.isStackable())
				{
						if (!activeChar.destroyItemByItemId("CTF Paticipation Fee", itemId, itemCount, activeChar.getTarget(), true))
						{
							activeChar.sendMessage("You do not have enough items, registration failed!");
							CTF.getInstance().remove(activeChar);
							return false;
						}
				}
					else
						for (int i = 0; i < itemCount; i++)
							activeChar.destroyItemByItemId("CTF Paticipation Fee", itemId, 1, activeChar.getTarget(), true);
					activeChar.sendMessage(String.format(Message.getMessage(activeChar, Message.MessageId.MSG_EVENT_REGISTERED), "CTF"));
					return true;
				}
			}
        }
        else if (command.equals("ctfleave"))
        {
            if ((CTF.getInstance().getState() == GameEvent.STATE_ACTIVE) && CTF.getInstance().isParticipant(activeChar))
            {
                CTF.getInstance().remove(activeChar);
                activeChar.sendMessage(String.format(Message.getMessage(activeChar, Message.MessageId.MSG_EVENT_CANCEL_REG), "CTF"));
            }
            else
            {
                activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_EVENT_NOT_REGISTERED));
            }
            return true;
        }
        return false;
    }

    @Override
    public String getDescription(String command)
    {
        if (command.equals("ctfjoin"))
        {
            return "Join the tournament CTF.";
        }
        if (command.equals("ctfleave"))
        {
            return "Cancel registration in tournament CTF.";
        }
        return null;
    }
}