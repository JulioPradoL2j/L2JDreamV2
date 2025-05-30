package com.dream.game.handler.user;

import com.dream.game.handler.IUserCommandHandler;
import com.dream.game.model.L2CommandChannel;
import com.dream.game.model.L2Party;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;

public class ChannelCreate implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		92
	};

	private static void tryCreateMPCC(L2PcInstance requestor)
	{
		boolean hasRight = false;
		if (requestor.getClan() != null && requestor.getClan().getLeaderId() == requestor.getObjectId() && requestor.getClan().getLevel() >= 5)
		{
			hasRight = true;
		}
		else
		{
			for (L2Skill skill : requestor.getAllSkills())
				if (skill.getId() == 391)
				{
					hasRight = true;
					break;
				}
		}
		if (!hasRight)
			if (requestor.destroyItemByItemId("MPCC", 8871, 1, requestor, false))
			{
				hasRight = true;
				requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(8871).addNumber(1));
			}
		if (!hasRight)
		{
			requestor.sendPacket(SystemMessageId.COMMAND_CHANNEL_ONLY_BY_LEVEL_5_CLAN_LEADER_PARTY_LEADER);
			return;
		}

		if (!requestor.isProcessingRequest())
		{
			new L2CommandChannel(requestor);
			requestor.getParty().broadcastToPartyMembers(SystemMessage.getSystemMessage(SystemMessageId.JOINED_COMMAND_CHANNEL));
		}
		else
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(requestor.getName()));
		}
	}

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
		if (activeChar == null)
			return false;
		if (activeChar.isInParty())
		{
			L2Party activeParty = activeChar.getParty();
			if (activeParty.getLeader() == activeChar)
			{
				if (!activeParty.isInCommandChannel())
				{
					tryCreateMPCC(activeChar);
				}
				else
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_MEMBER_OF_COMMAND_CHANNEL).addString(activeChar.getName()));
				}
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_INVITE_TO_COMMAND_CHANNEL);
			}

		}
		return true;
	}
}