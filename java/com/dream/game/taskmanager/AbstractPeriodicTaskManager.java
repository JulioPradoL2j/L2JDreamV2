package com.dream.game.taskmanager;

import org.apache.log4j.Logger;

import com.dream.game.L2GameServer;
import com.dream.game.L2GameServer.StartupHook;
import com.dream.game.network.ThreadPoolManager;
import com.dream.tools.random.Rnd;

abstract class AbstractPeriodicTaskManager implements Runnable, StartupHook
{
	static final Logger _log = Logger.getLogger(AbstractPeriodicTaskManager.class);

	private final int _period;

	AbstractPeriodicTaskManager(int period)
	{
		_period = period;

		L2GameServer.addStartupHook(this);
	}

	@Override
	public final void onStartup()
	{
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this, 1000 + Rnd.get(_period), Rnd.get(_period - 5, _period + 5));
	}

	public final void readLock()
	{

	}

	public final void readUnlock()
	{

	}

	@Override
	public abstract void run();

	public final void writeLock()
	{

	}

	public final void writeUnlock()
	{

	}
}
