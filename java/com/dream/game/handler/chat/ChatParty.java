package com.dream.game.handler.chat;

import com.dream.game.handler.IChatHandler;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.CreatureSay;

public class ChatParty implements IChatHandler
{
	private final SystemChatChannelId[] _chatTypes =
	{
		SystemChatChannelId.Chat_Party
	};

	@Override
	public SystemChatChannelId[] getChatTypes()
	{
		return _chatTypes;
	}

	@Override
	public void useChatHandler(L2PcInstance activeChar, String target, SystemChatChannelId chatType, String text)
	{
		if (activeChar.getSecondRefusal())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.isInParty())
		{
			activeChar.getParty().broadcastCSToPartyMembers(new CreatureSay(activeChar.getObjectId(), chatType, activeChar.getName(), text), activeChar);
			activeChar.getParty().broadcastSnoopToPartyMembers(chatType.getId(), activeChar.getName(), text);
		}
	}
}