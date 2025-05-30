package com.dream.game.handler.chat;

import com.dream.Config;
import com.dream.game.handler.IChatHandler;
import com.dream.game.model.BlockList;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.CreatureSay;

public class ChatAll implements IChatHandler
{
	private final SystemChatChannelId[] _chatTypes =
	{
		SystemChatChannelId.Chat_Normal
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

		String name = activeChar.getAppearance().getVisibleName();
		for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
			if (player != null && activeChar.isInsideRadius(player, 1250, false, true) && !(Config.REGION_CHAT_ALSO_BLOCKED && BlockList.isBlocked(player, activeChar)))
			{
				player.sendPacket(new CreatureSay(activeChar.getObjectId(), chatType, name, text));
				player.broadcastSnoop(chatType.getId(), name, text);
			}
		activeChar.sendPacket(new CreatureSay(activeChar.getObjectId(), chatType, name, text));
		activeChar.broadcastSnoop(chatType.getId(), name, text);
	}
}