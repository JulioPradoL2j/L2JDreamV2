package com.dream.game.model.entity.events.archievements;

import com.dream.game.model.actor.instance.L2PcInstance;

public class Castle extends Condition
{
  public Castle(Object value)
  {
    super(value);
    setName("Have Castle");
  }

  @Override
 public boolean meetConditionRequirements(L2PcInstance player)
  {
    if (getValue() == null) {
      return false;
    }
    if (player.getClan() != null)
    {
      if ((player.isCastleLord(5)) || (player.isCastleLord(3)) || (player.isCastleLord(7)))
        return true;
    }
    return false;
  }
}