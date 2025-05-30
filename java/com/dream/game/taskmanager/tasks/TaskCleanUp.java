package com.dream.game.taskmanager.tasks;

import org.apache.log4j.Logger;

import com.dream.game.manager.TaskManager.ExecutedTask;
import com.dream.game.taskmanager.Task;

public final class TaskCleanUp extends Task
{
	public static final String NAME = "clean_up";
	protected static final Logger _log = Logger.getLogger(TaskCleanUp.class.getName());

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		_log.info("Executing clean up task");
		System.gc();
		System.runFinalization();
		_log.info("RAM Used: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576);
	}
}