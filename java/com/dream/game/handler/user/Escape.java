package com.dream.game.handler.user;

import com.dream.Message;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.handler.IUserCommandHandler;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.PlaySound;

public class Escape implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		52
	};

	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}

	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (activeChar.isInsideZone(L2Zone.FLAG_NOESCAPE))
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_YOU_CANNOT_ESCAPE));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		if (activeChar.isCastingNow() || activeChar.isSitting() || activeChar.isMovementDisabled() || activeChar.isOutOfControl() || activeChar.isInOlympiadMode() || activeChar.inObserverMode() || activeChar.isFestivalParticipant() || activeChar.isInJail())
		{
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Your current state doesn't allow you to use the /unstuck command.");
			return false;
		}
		if (activeChar.isInFunEvent())
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_YOU_CANNOT_ESCAPE));
			return false;
		}
		activeChar.stopMove(null);

		// Official timer 5 minutes, for GM 1 second
		if (activeChar.isGM())
		{
			activeChar.doCast(SkillTable.getInstance().getInfo(2100, 1));
		}
		else
		{
			activeChar.sendPacket(new PlaySound("systemmsg_e.809"));
			activeChar.sendPacket(SystemMessageId.STUCK_TRANSPORT_IN_FIVE_MINUTES);

			activeChar.doCast(SkillTable.getInstance().getInfo(2099, 1));
		}

		return true;
	}
}