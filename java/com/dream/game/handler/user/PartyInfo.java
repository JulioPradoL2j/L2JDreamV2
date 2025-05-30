package com.dream.game.handler.user;

import com.dream.Message;
import com.dream.game.handler.IUserCommandHandler;
import com.dream.game.model.L2Party;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;

public class PartyInfo implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		81
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

		if (!activeChar.isInParty())
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_NO_PARTY));
			return false;
		}

		L2Party playerParty = activeChar.getParty();
		int memberCount = playerParty.getMemberCount();
		int lootDistribution = playerParty.getLootDistribution();
		String partyLeader = playerParty.getPartyMembers().get(0).getName();

		activeChar.sendPacket(SystemMessageId.PARTY_INFORMATION);
		switch (lootDistribution)
		{
			case L2Party.ITEM_LOOTER:
				activeChar.sendPacket(SystemMessageId.LOOTING_FINDERS_KEEPERS);
				break;
			case L2Party.ITEM_ORDER:
				activeChar.sendPacket(SystemMessageId.LOOTING_BY_TURN);
				break;
			case L2Party.ITEM_ORDER_SPOIL:
				activeChar.sendPacket(SystemMessageId.LOOTING_BY_TURN_INCLUDE_SPOIL);
				break;
			case L2Party.ITEM_RANDOM:
				activeChar.sendPacket(SystemMessageId.LOOTING_RANDOM);
				break;
			case L2Party.ITEM_RANDOM_SPOIL:
				activeChar.sendPacket(SystemMessageId.LOOTING_RANDOM_INCLUDE_SPOIL);
				break;
		}

		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PARTY_LEADER_S1).addString(partyLeader));
		activeChar.sendMessage(String.format(Message.getMessage(activeChar, Message.MessageId.MSG_PARTY_MEMBERS_COUNT), memberCount));
		activeChar.sendPacket(SystemMessageId.WAR_LIST);
		return true;
	}
}