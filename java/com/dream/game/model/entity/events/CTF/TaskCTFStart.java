package com.dream.game.model.entity.events.CTF;

import org.apache.log4j.Logger;

import com.dream.game.manager.TaskManager.ExecutedTask;
import com.dream.game.model.entity.events.GameEvent;
import com.dream.game.taskmanager.Task;

public final class TaskCTFStart extends Task
{
    private static final Logger _log = Logger.getLogger(TaskCTFStart.class.getName());

    @Override
    public String getName()
    {
        return CTF.getInstance().getName();
    }

    @Override
    public void onTimeElapsed(ExecutedTask task)
    {
        if (CTF.getInstance() != null)
        {
            if (CTF.getInstance().getState() == GameEvent.STATE_INACTIVE)
            {
                CTF.getInstance().start();
                _log.info("CTF Event started by Global Task Manager");
            }
        }
    }
}