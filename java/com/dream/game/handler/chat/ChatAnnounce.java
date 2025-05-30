package com.dream.game.handler.chat;

import com.dream.game.handler.IChatHandler;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.CreatureSay;

public class ChatAnnounce implements IChatHandler
{
	private final SystemChatChannelId[] _chatTypes =
	{
		SystemChatChannelId.Chat_Announce,
		SystemChatChannelId.Chat_Critical_Announce
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


		if (activeChar != null)
		{
			charName = activeChar.getName();
			charObjId = activeChar.getObjectId();

			if (activeChar.getSecondRefusal())
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (!activeChar.isGM())
				return;
		}

		if (chatType == SystemChatChannelId.Chat_Critical_Announce)
		{
			text = "** " + text;
		}

		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			if (player != null)
			{
				player.sendPacket(new CreatureSay(charObjId, chatType, charName, text));
			}
	}
}