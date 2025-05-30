package com.dream.game.handler.user;

import com.dream.game.handler.IUserCommandHandler;
import com.dream.game.model.L2CommandChannel;
import com.dream.game.model.L2Party;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;

public class ChannelLeave implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		96
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
			if (activeChar.getParty().isLeader(activeChar) && activeChar.getParty().isInCommandChannel())
			{
				L2CommandChannel channel = activeChar.getParty().getCommandChannel();
				L2Party party = activeChar.getParty();
				channel.removeParty(party);

				party.getLeader().sendPacket(SystemMessageId.LEFT_COMMAND_CHANNEL);
				channel.broadcastToChannelMembers(SystemMessage.getSystemMessage(SystemMessageId.S1_PARTY_LEFT_COMMAND_CHANNEL).addPcName(party.getLeader()));
				return true;
			}
		}
		return false;
	}
}