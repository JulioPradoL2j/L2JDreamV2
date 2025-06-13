package com.dream.game.handler.chat;

import com.dream.Message;
import com.dream.game.handler.IChatHandler;
import com.dream.game.model.BlockList;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.util.FloodProtector;
import com.dream.game.util.FloodProtector.Protected;

public class ChatHero implements IChatHandler
{
	private final SystemChatChannelId[] _chatTypes =
	{
		SystemChatChannelId.Chat_Hero
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
		
		boolean isGM = activeChar.isGM();
		boolean isVIP = activeChar.isVip();
		boolean isHero = activeChar.isHero();
		
		boolean canSpeak = false;
		
		if (isGM)
		{
			canSpeak = true;
		}
		else if (isVIP || isHero)
		{
			if (FloodProtector.tryPerformAction(activeChar, Protected.HEROVOICE))
			{
				canSpeak = true;
			}
			else
			{
				activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_HERO_CHAT_ONCE_PER_TEN_SEC));
			}
		}
		
		if (canSpeak)
		{
			String prefix = "";
			if (isGM)
			{
				prefix = "[GM] ";
			}
			else if (isVIP)
			{
				prefix = "[VIP] ";
			}
			
			String senderName = prefix + activeChar.getName();
			
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				if (player != null && !BlockList.isBlocked(player, activeChar))
				{
					player.sendPacket(new CreatureSay(activeChar.getObjectId(), chatType, senderName, text));
				}
			}
		}
	}
}