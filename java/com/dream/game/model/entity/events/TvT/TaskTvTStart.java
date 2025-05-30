package com.dream.game.model.entity.events.TvT;

import org.apache.log4j.Logger;

import com.dream.game.manager.TaskManager.ExecutedTask;
import com.dream.game.model.entity.events.GameEvent;
import com.dream.game.taskmanager.Task;

public final class TaskTvTStart extends Task
{
	private static final Logger _log = Logger.getLogger(TaskTvTStart.class.getName());

	@Override
	public String getName()
	{
		return TvT.getInstance().getName();
	}

	@Override
	public void onTimeElapsed(ExecutedTask task)
	{

		if (TvT.getInstance() == null || TvT.getInstance().getState() != GameEvent.STATE_INACTIVE)
			return;
		_log.info("TvT Event started by Global Task Manager");
		TvT.getInstance().start();
	}
}