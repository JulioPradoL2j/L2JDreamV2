package com.dream.game.taskmanager;

import java.util.concurrent.ScheduledFuture;

import com.dream.game.manager.TaskManager.ExecutedTask;

public abstract class Task
{
	public abstract String getName();

	public void initializate()
	{

	}

	public ScheduledFuture<?> launchSpecial(ExecutedTask instance)
	{
		return null;
	}

	public void onDestroy()
	{
	}

	public abstract void onTimeElapsed(ExecutedTask task);
}