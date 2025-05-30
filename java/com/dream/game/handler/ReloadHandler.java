package com.dream.game.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemChatChannelId;

public class ReloadHandler
{
	private static ReloadHandler _instance = null;

	public static ReloadHandler getInstance()
	{
		if (_instance == null)
		{
			_instance = new ReloadHandler();
		}
		return _instance;
	}

	private final Map<String, IReloadHandler> _handlers = new HashMap<>();

	private ReloadHandler()
	{

	}

	public Set<String> getHandlers()
	{
		return _handlers.keySet();
	}

	public boolean isRegistred(String handler)
	{
		return _handlers.containsKey(handler);
	}

	public void registerHandler(String name, IReloadHandler handler)
	{
		_handlers.put(name, handler);
	}

	public void reload(String handler, L2PcInstance actor)
	{
		IReloadHandler h = _handlers.get(handler);
		if (h != null)
		{
			h.reload(actor);
		}
		else
		{
			actor.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "//reload " + handler + " not define.");
		}
	}

}