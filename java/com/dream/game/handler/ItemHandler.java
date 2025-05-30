package com.dream.game.handler;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.dream.util.JarUtils;

public class ItemHandler
{
	private final static Logger _log = Logger.getLogger(ItemHandler.class.getName());

	private static ItemHandler _instance;

	public static ItemHandler getInstance()
	{
		if (_instance == null)
		{
			_instance = new ItemHandler();
		}
		return _instance;
	}

	private final Map<Integer, IItemHandler> _datatable;

	private final Map<Integer, IExItemHandler> _exHandlers = new HashMap<>();

	private ItemHandler()
	{
		_datatable = new TreeMap<>();
		try
		{
			for (String handler : JarUtils.enumClasses("com.dream.game.handler.item"))
			{
				try
				{
					Class<?> _handler = Class.forName(handler);
					if (_handler != null && IItemHandler.class.isAssignableFrom(_handler))
					{
						Constructor<?> ctor = _handler.getConstructor();
						if (ctor != null)
						{
							registerItemHandler((IItemHandler) ctor.newInstance());
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

		_log.info("Item Handler: Loaded " + _datatable.size() + " handler(s).");
	}

	public IExItemHandler getExHandler(int id)
	{
		return _exHandlers.get(id);
	}

	public IItemHandler getItemHandler(int itemId)
	{
		return _datatable.get(itemId);
	}

	public void registerExHandler(IExItemHandler handler)
	{
		for (int i : handler.getItemIds())
		{
			_exHandlers.put(i, handler);
		}
	}

	public void registerItemHandler(IItemHandler handler)
	{
		int[] ids = handler.getItemIds();
		for (int element : ids)
		{
			_datatable.put(element, handler);
		}
	}

}