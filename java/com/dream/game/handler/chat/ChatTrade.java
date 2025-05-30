package com.dream.game.handler.chat;

import com.dream.Config;
import com.dream.Config.ChatMode;
import com.dream.Message;
import com.dream.game.datatables.xml.MapRegionTable;
import com.dream.game.handler.IChatHandler;
import com.dream.game.model.BlockList;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.mapregion.L2MapRegion;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.util.FloodProtector;
import com.dream.game.util.FloodProtector.Protected;

public class ChatTrade implements IChatHandler
{
	private final SystemChatChannelId[] _chatTypes =
	{
		SystemChatChannelId.Chat_Market
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

		if (!FloodProtector.tryPerformAction(activeChar, Protected.TRADE_CHAT) && !activeChar.isGM())
		{
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(activeChar, Message.MessageId.MSG_CHAT_FLOOD_PROTECT));
			return;
		}
		if (activeChar.getLevel() < Config.TRADE_CHAT_LEVEL)
		{
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Trade chat is available from " + Config.TRADE_CHAT_LEVEL + " level");
			return;
		}

		if (Config.DEFAULT_TRADE_CHAT == ChatMode.REGION)
		{
			L2MapRegion region = MapRegionTable.getInstance().getRegion(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
				if (region == MapRegionTable.getInstance().getRegion(player.getX(), player.getY(), player.getZ()) && !(Config.REGION_CHAT_ALSO_BLOCKED && BlockList.isBlocked(player, activeChar)))
				{
					player.sendPacket(new CreatureSay(activeChar.getObjectId(), chatType, activeChar.getName(), text));
					player.broadcastSnoop(chatType.getId(), activeChar.getName(), text);
				}
		}
		else if (Config.DEFAULT_TRADE_CHAT == ChatMode.GLOBAL || Config.DEFAULT_TRADE_CHAT == ChatMode.GM && activeChar.isGM())
		{
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
				if (!(Config.REGION_CHAT_ALSO_BLOCKED && BlockList.isBlocked(player, activeChar)))
				{
					player.sendPacket(new CreatureSay(activeChar.getObjectId(), chatType, activeChar.getName(), text));
					player.broadcastSnoop(chatType.getId(), activeChar.getName(), text);
				}
		}
	}
}