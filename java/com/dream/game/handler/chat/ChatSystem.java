package com.dream.game.handler.chat;

import com.dream.game.handler.IChatHandler;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.serverpackets.CreatureSay;

public class ChatSystem implements IChatHandler
{
	private final SystemChatChannelId[] _chatTypes =
	{
		SystemChatChannelId.Chat_System
	};

	@Override
	public SystemChatChannelId[] getChatTypes()
	{
		return _chatTypes;
	}

	@Override
	public void useChatHandler(L2PcInstance activeChar, String target, SystemChatChannelId chatType, String text)
	{
		for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
			if (player != null && activeChar.isInsideRadius(player, 1250, false, true))
			{
				player.sendPacket(new CreatureSay(activeChar.getObjectId(), chatType, activeChar.getName() + "'s Emote", text));
				player.broadcastSnoop(chatType.getId(), activeChar.getName(), text);
			}
		activeChar.sendPacket(new CreatureSay(activeChar.getObjectId(), chatType, activeChar.getName() + "'s Emote", text));
		activeChar.broadcastSnoop(chatType.getId(), activeChar.getName(), text);
	}
}