package com.dream.thread;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public final class RunnableStatsManager
{
	private class ClassStat
	{
		public final Class<?> clazz;
		public long runCount = 0;
		public long runTime = 0;
		public long minTime = Long.MAX_VALUE;
		public long maxTime = Long.MIN_VALUE;

		public ClassStat(Class<?> cl)
		{
			clazz = cl;
			classStats.put(cl, this);
		}
	}

	private static final Logger _log = Logger.getLogger(RunnableStatsManager.class);

	private static RunnableStatsManager _instance;

	public static RunnableStatsManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new RunnableStatsManager();
		}

		return _instance;
	}

	public final Map<Class<?>, ClassStat> classStats = new HashMap<>();

	private long _lastClassDump;

	private List<ClassStat> getSortedClassStats()
	{
		List<ClassStat> result = Arrays.asList(classStats.values().toArray(new ClassStat[classStats.size()]));

		Collections.sort(result, new Comparator<ClassStat>()
		{
			@Override
			public int compare(ClassStat c1, ClassStat c2)
			{
				if (c1.maxTime < c2.maxTime)
					return 1;
				if (c1.maxTime == c2.maxTime)
					return 0;
				return -1;
			}
		});
		return result;
	}

	public synchronized CharSequence getStats()
	{
		StringBuilder list = new StringBuilder();

		String newline = System.getProperty("line.separator");

		if (_lastClassDump + 1000 > System.currentTimeMillis())
			return list;

		try
		{
			List<ClassStat> stats = getSortedClassStats();

			for (ClassStat stat : stats)
			{
				list.append(stat.clazz.getName().replace("net.sf.l2j.gameserver.", "") + ":" + newline);

				list.append("\tCount: .......... " + stat.runCount + newline);
				list.append("\tTime: ........... " + stat.runTime + newline);
				list.append("\tMin: ............ " + stat.minTime + newline);
				list.append("\tMax: ............ " + stat.maxTime + newline);
				list.append("\tAverage: ........ " + stat.runTime / stat.runCount + newline);
			}
		}
		catch (Exception e)
		{
			_log.warn("", e);
		}

		return list;
	}

	public synchronized void handleStats(Class<?> cl, long runTime)
	{
		ClassStat stat = classStats.get(cl);

		if (stat == null)
		{
			stat = new ClassStat(cl);
		}

		stat.runCount++;
		stat.runTime += runTime;
		if (stat.minTime > runTime)
		{
			stat.minTime = runTime;
		}
		if (stat.maxTime < runTime)
		{
			stat.maxTime = runTime;
		}
	}
}