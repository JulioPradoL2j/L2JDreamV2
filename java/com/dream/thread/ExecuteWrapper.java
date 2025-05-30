package com.dream.thread;

import org.apache.log4j.Logger;

public final class ExecuteWrapper implements Runnable
{
	private static final Logger _log = Logger.getLogger(ExecuteWrapper.class.getName());

	public static void execute(Runnable runnable)
	{
		long begin = System.nanoTime();

		try
		{
			runnable.run();

			RunnableStatsManager.getInstance().handleStats(runnable.getClass(), System.nanoTime() - begin);
		}
		catch (Exception e)
		{
			_log.warn("Exception in a Runnable execution:", e);
		}
	}

	private final Runnable _runnable;

	public ExecuteWrapper(Runnable runnable)
	{
		_runnable = runnable;
	}

	@Override
	public void run()
	{
		ExecuteWrapper.execute(_runnable);
	}
}
