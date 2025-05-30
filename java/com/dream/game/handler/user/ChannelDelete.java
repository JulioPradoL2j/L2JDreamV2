package com.dream.game.handler.user;

import com.dream.game.handler.IUserCommandHandler;
import com.dream.game.model.L2CommandChannel;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;

public class ChannelDelete implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		93
	};

	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}

	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (id != COMMAND_IDS[0])
			return false;

		if (activeChar.isInParty())
		{
			if (activeChar.getParty().isLeader(activeChar) && activeChar.getParty().isInCommandChannel() && activeChar.getParty().getCommandChannel().getChannelLeader() == activeChar)
			{
				L2CommandChannel channel = activeChar.getParty().getCommandChannel();

				channel.broadcastToChannelMembers(SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_DISBANDED));
				channel.disbandChannel();
				return true;
			}
			activeChar.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_LEAVE_CHANNEL);
		}

		return false;
	}
}