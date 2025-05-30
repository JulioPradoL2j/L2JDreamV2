package com.dream.game.handler;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dream.util.JarUtils;

public class UserCommandHandler
{
	private final static Logger _log = Logger.getLogger(UserCommandHandler.class.getName());

	private static UserCommandHandler _instance;

	public static UserCommandHandler getInstance()
	{
		if (_instance == null)
		{
			_instance = new UserCommandHandler();
		}
		return _instance;
	}

	private final Map<Integer, IUserCommandHandler> _datatable;

	private UserCommandHandler()
	{
		_datatable = new HashMap<>();
		try
		{
			for (String handler : JarUtils.enumClasses("com.dream.game.handler.user"))
			{
				try
				{
					Class<?> _handler = Class.forName(handler);
					if (_handler != null && IUserCommandHandler.class.isAssignableFrom(_handler))
					{
						Constructor<?> ctor = _handler.getConstructor();
						if (ctor != null)
						{
							registerUserCommandHandler((IUserCommandHandler) ctor.newInstance());
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

		_log.info("User Handler: Loaded " + _datatable.size() + " handler(s).");
	}

	public IUserCommandHandler getUserCommandHandler(int userCommand)
	{
		return _datatable.get(Integer.valueOf(userCommand));
	}

	public void registerUserCommandHandler(IUserCommandHandler handler)
	{
		int[] ids = handler.getUserCommandList();
		for (int element : ids)
		{
			_datatable.put(element, handler);
		}
	}

	public int size()
	{
		return _datatable.size();
	}

}