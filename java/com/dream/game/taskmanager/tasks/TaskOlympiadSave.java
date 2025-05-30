package com.dream.game.taskmanager.tasks;

import org.apache.log4j.Logger;

import com.dream.game.manager.TaskManager;
import com.dream.game.manager.TaskManager.ExecutedTask;
import com.dream.game.model.olympiad.Olympiad;
import com.dream.game.taskmanager.Task;
import com.dream.game.taskmanager.TaskTypes;

public class TaskOlympiadSave extends Task
{
	private static final Logger _log = Logger.getLogger(TaskOlympiadSave.class.getName());
	public static final String NAME = "olympiad_save";

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, "900000", "1800000", "");
	}

	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		if (Olympiad.inCompPeriod())
		{
			Olympiad.getInstance().saveOlympiadStatus();
			_log.info("Olympiad System: Data updated successfully.");
		}
	}
}