package com.dream.game.taskmanager.tasks;

import com.dream.Config;
import com.dream.game.Shutdown;
import com.dream.game.manager.TaskManager;
import com.dream.game.manager.TaskManager.ExecutedTask;
import com.dream.game.taskmanager.Task;
import com.dream.game.taskmanager.TaskTypes;

public final class TaskRestart extends Task
{
	public static final String NAME = "restart";

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public void initializate()
	{
		if (Config.ENABLE_RESTART)
		{
			int timeInMin = Integer.parseInt(Config.RESTART_WARN_TIME);
			int finalTime = timeInMin * 60;
			String timer = "" + finalTime;
			super.initializate();
			TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", Config.RESTART_TIME, timer);
		}
	}

	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		if (Config.ENABLE_RESTART)
		{
			Shutdown handler = new Shutdown(Integer.valueOf(task.getParams()[2]), Shutdown.ShutdownModeType.RESTART);
			handler.start();
		}
	}
}