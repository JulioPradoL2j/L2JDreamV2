package com.dream.game.network.clientpackets;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.handler.ChatHandler;
import com.dream.game.handler.IChatHandler;
import com.dream.game.handler.IVoicedCommandHandler;
import com.dream.game.handler.VoicedCommandHandler;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;
import com.dream.game.util.FloodProtector;
import com.dream.game.util.FloodProtector.Protected;

public class Say2 extends L2GameClientPacket
{
	private static Logger _logChat = Logger.getLogger("chat");
	private SystemChatChannelId _chat;
	private String _text;
	private String _target;
	private int _type;

	private void checkText(L2PcInstance activeChar)
	{
		String filteredText = _text;
		for (Pattern pattern : Config.FILTER_LIST)
		{
			filteredText = pattern.matcher(filteredText).replaceAll(Config.CHAT_FILTER_CHARS);
		}

		if (Config.KARMA_ON_OFFENSIVE > 0 && !_text.equals(filteredText))
		{
			activeChar.setKarma(activeChar.getKarma() + Config.KARMA_ON_OFFENSIVE);
		}

		_text = filteredText;
	}

	@Override
	protected void readImpl()
	{
		_text = readS();
		_type = readD();
		_chat = SystemChatChannelId.getChatType(_type);
		_target = _chat == SystemChatChannelId.Chat_Tell ? readS() : null;
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
			return;

		if (_text.length() > Config.CHAT_LENGTH)
		{
			activeChar.sendMessage("Message length exceeded!");
			_text = _text.substring(0, Config.CHAT_LENGTH);
		}

		switch (_chat)
		{
			case Chat_None:
			case Chat_Announce:
			case Chat_Critical_Announce:
			case Chat_System:
			case Chat_Custom:
			case Chat_GM_Pet:
			{
				if (!activeChar.isGM())
				{
					_chat = SystemChatChannelId.Chat_Normal;
				}
			}
		}

		if (_chat == SystemChatChannelId.Chat_Normal && _text.startsWith(".") && !_text.startsWith(".."))
		{
			String[] _commandParams = _text.split(" ");
			String command = _commandParams[0].substring(1);
			String params = "";

			if (_commandParams.length > 1)
			{
				params = _text.substring(1 + command.length()).trim();
			}
			else if (activeChar.getTarget() != null)
			{
				params = activeChar.getTarget().getName();
			}

			IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);

			if (vch != null)
			{
				if (!activeChar.isGM() && !FloodProtector.tryPerformAction(activeChar, Protected.VOICE_CMD))
					return;
				vch.useVoicedCommand(command, activeChar, params);
				return;
			}
			return;
		}

		if (!activeChar.isGM() && (activeChar.isChatBanned() || activeChar.isInsideZone(L2Zone.FLAG_NOCHAT)))
			if (_chat != SystemChatChannelId.Chat_User_Pet && _chat != SystemChatChannelId.Chat_Tell)
			{
				activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
				return;
			}

		if (_chat == SystemChatChannelId.Chat_User_Pet && activeChar.isGM())
		{
			_chat = SystemChatChannelId.Chat_GM_Pet;
		}

		if (activeChar.isCursedWeaponEquipped())
		{
			switch (_chat)
			{
				case Chat_Shout:
				case Chat_Market:
					activeChar.sendMessage("Shout and trade chatting cannot be used while possessing a cursed weapon.");
					return;
			}
		}

		if (Config.LOG_CHAT)
			if (_chat == SystemChatChannelId.Chat_Tell)
			{
				_logChat.info(_chat.getName() + "[" + activeChar.getName() + " to " + _target + "] " + _text);
			}
			else
			{
				_logChat.info(_chat.getName() + "[" + activeChar.getName() + "] " + _text);
			}

		if (Config.USE_SAY_FILTER)
		{
			switch (_chat)
			{
				case Chat_Normal:
				case Chat_Shout:
				case Chat_Market:
				case Chat_Tell:
				case Chat_Hero:
					checkText(activeChar);
			}
		}

		IChatHandler ich = ChatHandler.getInstance().getChatHandler(_chat);
		if (ich != null)
		{
			ich.useChatHandler(activeChar, _target, _chat, _text);
		}
	}

}