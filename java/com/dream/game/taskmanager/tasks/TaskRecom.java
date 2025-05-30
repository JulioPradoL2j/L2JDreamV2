package com.dream.game.taskmanager.tasks;

import org.apache.log4j.Logger;

import com.dream.game.manager.TaskManager;
import com.dream.game.manager.TaskManager.ExecutedTask;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.taskmanager.Task;
import com.dream.game.taskmanager.TaskTypes;

public class TaskRecom extends Task
{
	private static final Logger _log = Logger.getLogger(TaskRecom.class.getName());
	private static final String NAME = "sp_recommendations";

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "13:00:00", "");
	}

	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			player.checkRecom(player.getRecomHave(), player.getRecomLeft());
			player.broadcastUserInfo();
		}
		_log.info("Recommendation Global Task: launched.");
	}
}