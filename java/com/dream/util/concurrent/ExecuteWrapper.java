package com.dream.util.concurrent;

public class ExecuteWrapper implements Runnable
{
	public static void execute(Runnable runnable)
	{
		execute(runnable, Long.MAX_VALUE);
	}

	public static void execute(Runnable runnable, long maximumRuntimeInMillisecWithoutWarning)
	{
		long begin = System.nanoTime();

		try
		{
			runnable.run();
		}
		catch (Exception e)
		{
		}
		finally
		{
			long runtimeInNanosec = System.nanoTime() - begin;
			Class<? extends Runnable> clazz = runnable.getClass();

			RunnableStatsManager.getInstance().handleStats(clazz, runtimeInNanosec);
		}
	}

	private final Runnable _runnable;

	public ExecuteWrapper(Runnable runnable)
	{
		_runnable = runnable;
	}

	protected long getMaximumRuntimeInMillisecWithoutWarning()
	{
		return Long.MAX_VALUE;
	}

	@Override
	public final void run()
	{
		ExecuteWrapper.execute(_runnable, getMaximumRuntimeInMillisecWithoutWarning());
	}
}