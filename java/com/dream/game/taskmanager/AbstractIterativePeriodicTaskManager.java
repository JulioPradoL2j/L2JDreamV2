package com.dream.game.taskmanager;

import java.util.Set;

import com.dream.Config;
import com.dream.util.concurrent.RunnableStatsManager;

import javolution.util.FastSet;

public abstract class AbstractIterativePeriodicTaskManager<T> extends AbstractPeriodicTaskManager
{
	private final Set<T> _startList = new FastSet<>();
	private final Set<T> _stopList = new FastSet<>();

	private final FastSet<T> _activeTasks = new FastSet<>();

	protected AbstractIterativePeriodicTaskManager(int period)
	{
		super(period);
	}

	protected abstract void callTask(T task);

	protected abstract String getCalledMethodName();

	public boolean hasTask(T task)
	{
		synchronized (_activeTasks)
		{
			if (_stopList.contains(task))
				return false;

			return _activeTasks.contains(task) || _startList.contains(task);
		}
	}

	protected void onStopTask(T task)
	{
	}

	@Override
	public final void run()
	{
		synchronized (_activeTasks)
		{
			_activeTasks.addAll(_startList);
			_activeTasks.removeAll(_stopList);

			_startList.clear();
			_stopList.clear();
		}

		for (FastSet.Record r = _activeTasks.head(), end = _activeTasks.tail(); (r = r.getNext()) != end;)
		{
			final T task = _activeTasks.valueOf(r);
			final long begin = System.nanoTime();

			try
			{
				callTask(task);
			}
			catch (RuntimeException e)
			{
				if (Config.DEBUG)
				{
					_log.warn("", e);
				}
			}
			finally
			{
				RunnableStatsManager.getInstance().handleStats(task.getClass(), getCalledMethodName(), System.nanoTime() - begin);
			}
		}
	}

	public final void startTask(T task)
	{
		synchronized (_activeTasks)
		{
			_startList.add(task);

			_stopList.remove(task);
		}
	}

	public final void stopTask(T task)
	{
		synchronized (_activeTasks)
		{
			_stopList.add(task);
			_startList.remove(task);
			onStopTask(task);
		}
	}
}
