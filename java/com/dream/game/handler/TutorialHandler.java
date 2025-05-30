package com.dream.game.handler;

import java.util.HashMap;
import java.util.Map;

import com.dream.game.handler.tutorialhandlers.StartupHandler;

public class TutorialHandler
{
	private final Map<Integer, ITutorialHandler> _datatable;

	public static TutorialHandler getInstance()
	{
		return SingletonHolder._instance;
	}

	protected TutorialHandler()
	{
		_datatable = new HashMap<>();

		registerHandler(new StartupHandler());
	}

	public void registerHandler(ITutorialHandler handler)
	{
		String[] ids = handler.getLinkList();

		for (int i = 0; i < ids.length; i++)
		{
			_datatable.put(ids[i].hashCode(), handler);
		}
	}

	public ITutorialHandler getHandler(String link)
	{
		String command = link;

		if (link.indexOf("_") != -1)
			command = link.substring(0, link.indexOf("_"));

		return _datatable.get(command.hashCode());
	}

	public int size()
	{
		return _datatable.size();
	}

	private static class SingletonHolder
	{
		protected static final TutorialHandler _instance = new TutorialHandler();
	}
}