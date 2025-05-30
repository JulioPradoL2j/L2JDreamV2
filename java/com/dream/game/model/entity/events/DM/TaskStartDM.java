package com.dream.game.model.entity.events.DM;

import com.dream.game.manager.TaskManager.ExecutedTask;
import com.dream.game.model.entity.events.GameEvent;
import com.dream.game.taskmanager.Task;

public class TaskStartDM extends Task
{

    @Override
    public String getName()
    {
        return DeathMatch.getInstance().getName();
    }

    @Override
    public void onTimeElapsed(ExecutedTask task)
    {
        if ((DeathMatch.getInstance() != null) && (DeathMatch.getInstance().getState() == GameEvent.STATE_INACTIVE))
        {
            DeathMatch.getInstance().start();
        }
    }
}