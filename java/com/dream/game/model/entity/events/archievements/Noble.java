package com.dream.game.model.entity.events.archievements;

import com.dream.game.model.actor.instance.L2PcInstance;

public class Noble extends Condition
{
  public Noble(Object value)
  {
    super(value);
    setName("Noble");
  }

  @Override
public boolean meetConditionRequirements(L2PcInstance player)
  {
    if (getValue() == null) {
      return false;
    }

    return player.isNoble();
  }
}