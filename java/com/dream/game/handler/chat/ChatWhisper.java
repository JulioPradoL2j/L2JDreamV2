package com.dream.game.handler.chat;

import com.dream.Message;
import com.dream.game.handler.IChatHandler;
import com.dream.game.model.BlockList;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.CreatureSay;

public class ChatWhisper implements IChatHandler
{
	private final SystemChatChannelId[] _chatTypes =
	{
		SystemChatChannelId.Chat_Tell
	};

	@Override
	public SystemChatChannelId[] getChatTypes()
	{
		return _chatTypes;
	}

	@Override
	public void useChatHandler(L2PcInstance activeChar, String target, SystemChatChannelId chatType, String text)
	{
		L2PcInstance receiver = L2World.getInstance().getPlayer(target);

		if (activeChar.getSecondRefusal())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (receiver != null && !BlockList.isBlocked(receiver, activeChar))
		{
			if (!receiver.getMessageRefusal() || activeChar.isGM())
			{
				if (receiver.isOfflineTrade())
				{
					activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_CHAR_BUSY_TRY_LATER));
					return;
				}
				if (receiver.isInJail() && !activeChar.isGM())
				{
					activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_CHAR_BUSY_TRY_LATER));
					return;
				}
				receiver.sendPacket(new CreatureSay(activeChar.getObjectId(), chatType, activeChar.getName(), text));
				receiver.broadcastSnoop(chatType.getId(), activeChar.getName(), text);
				activeChar.sendPacket(new CreatureSay(activeChar.getObjectId(), chatType, "->" + receiver.getName(), text));
				activeChar.broadcastSnoop(chatType.getId(), "->" + receiver.getName(), text);
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
			}
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
		}
	}
}