package com.dream.game.handler.chat;

import com.dream.game.handler.IChatHandler;
import com.dream.game.model.L2CommandChannel;
import com.dream.game.model.L2Party;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.CreatureSay;

public class ChatPartyRoom implements IChatHandler
{
	private final SystemChatChannelId[] _chatTypes =
	{
		SystemChatChannelId.Chat_Party_Room
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
		if (party != null)
		{
			L2CommandChannel chan = party.getCommandChannel();
			if (chan != null && party.isLeader(activeChar))
			{
				chan.broadcastCSToChannelMembers(new CreatureSay(charObjId, chatType, charName, text), activeChar);
			}
		}
	}
}