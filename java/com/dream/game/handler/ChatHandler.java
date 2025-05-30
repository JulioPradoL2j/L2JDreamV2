package com.dream.game.handler;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dream.game.network.SystemChatChannelId;
import com.dream.util.JarUtils;

public class ChatHandler
{
	private final static Logger _log = Logger.getLogger(ChatHandler.class.getName());

	private static ChatHandler _instance = null;

	public static ChatHandler getInstance()
	{
		if (_instance == null)
		{
			_instance = new ChatHandler();
		}
		return _instance;
	}

	private final Map<SystemChatChannelId, IChatHandler> _datatable;

	public ChatHandler()
	{
		_datatable = new HashMap<>();
		try
		{
			for (String handler : JarUtils.enumClasses("com.dream.game.handler.chat"))
			{
				try
				{
					Class<?> _handler = Class.forName(handler);
					if (_handler != null && IChatHandler.class.isAssignableFrom(_handler))
					{
						Constructor<?> ctor = _handler.getConstructor();
						if (ctor != null)
						{
							registerChatHandler((IChatHandler) ctor.newInstance());
						}
					}
				}
				catch (Exception e)
				{
					continue;
				}
			}
		}
		catch (Exception e)
		{

		}

		_log.info("Chat Handler: Loaded " + _datatable.size() + " handler(s).");
	}

	public IChatHandler getChatHandler(SystemChatChannelId chatId)
	{
		return _datatable.get(chatId);
	}

	public void registerChatHandler(IChatHandler handler)
	{
		SystemChatChannelId chatId[] = handler.getChatTypes();

		for (SystemChatChannelId chat : chatId)
		{
			_datatable.put(chat, handler);
		}
	}

}