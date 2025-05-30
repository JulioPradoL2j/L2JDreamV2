package com.dream.game.taskmanager;

import com.dream.game.model.L2Object;
import com.dream.game.model.world.L2World;

public final class KnownListUpdateTaskManager extends AbstractPeriodicTaskManager
{
	private static KnownListUpdateTaskManager _instance;

	public static KnownListUpdateTaskManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new KnownListUpdateTaskManager();
		}

		return _instance;
	}

	public KnownListUpdateTaskManager()
	{
		super(10 * 60 * 1000);
	}

	@Override
	public void run()
	{
		for (L2Object obj : L2World.getInstance().getAllVisibleObjects())
		{
			obj.getKnownList().tryRemoveObjects(false);
		}
	}
}
