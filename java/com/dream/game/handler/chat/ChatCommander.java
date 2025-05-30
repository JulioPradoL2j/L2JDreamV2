package com.dream.game.handler.chat;

import com.dream.game.handler.IChatHandler;
import com.dream.game.model.L2Party;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.CreatureSay;

public class ChatCommander implements IChatHandler
{
	private final SystemChatChannelId[] _chatTypes =
	{
		SystemChatChannelId.Chat_Commander,
		SystemChatChannelId.Chat_Inner_Partymaster
	};

	@Override
	public SystemChatChannelId[] getChatTypes()
	{
		return _chatTypes;
	}

	@Override
	public void useChatHandler(L2PcInstance activeChar, String target, SystemChatChannelId chatType, String text)
	{
		String charName = "";
		int charObjId = 0;

		if (activeChar == null)
			return;

		if (activeChar.getSecondRefusal())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		charName = activeChar.getName();
		charObjId = activeChar.getObjectId();

		L2Party party = activeChar.getParty();
		if (party != null && party.isInCommandChannel())
			if (chatType == SystemChatChannelId.Chat_Commander)
			{
				if (party.getCommandChannel().getChannelLeader() == activeChar)
				{
					party.getCommandChannel().broadcastToChannelMembers(new CreatureSay(charObjId, chatType, charName, text));
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.ONLY_CHANNEL_CREATOR_CAN_GLOBAL_COMMAND);
				}
			}
			else if (chatType == SystemChatChannelId.Chat_Inner_Partymaster)
				if (party.getLeader() == activeChar)
				{
					party.getCommandChannel().broadcastCSToChannelMembers(new CreatureSay(charObjId, chatType, charName, text), activeChar);
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.COMMAND_CHANNEL_ONLY_FOR_PARTY_LEADER);
				}
	}
}