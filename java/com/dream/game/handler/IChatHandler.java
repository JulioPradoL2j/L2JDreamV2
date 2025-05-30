package com.dream.game.handler;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemChatChannelId;

public interface IChatHandler
{
	public SystemChatChannelId[] getChatTypes();

	public void useChatHandler(L2PcInstance activeChar, String target, SystemChatChannelId chatType, String text);

}