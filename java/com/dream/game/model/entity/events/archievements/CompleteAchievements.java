package com.dream.game.model.entity.events.archievements;

import com.dream.game.model.actor.instance.L2PcInstance;

public class CompleteAchievements extends Condition
{
  public CompleteAchievements(Object value)
  {
    super(value);
    setName("Complete Achievements");
  }

  @Override
public boolean meetConditionRequirements(L2PcInstance player)
  {
    if (getValue() == null) {
      return false;
    }
    int val = Integer.parseInt(getValue().toString());

    return player.getCompletedAchievements().size() >= val;
  }
}