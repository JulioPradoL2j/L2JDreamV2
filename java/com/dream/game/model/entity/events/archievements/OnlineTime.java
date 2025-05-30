package com.dream.game.model.entity.events.archievements;

import com.dream.game.model.actor.instance.L2PcInstance;

public class OnlineTime extends Condition
{
  public OnlineTime(Object value)
  {
    super(value);
    setName("Online Time");
  }

  @Override
public boolean meetConditionRequirements(L2PcInstance player)
  {
    if (getValue() == null) {
      return false;
    }
    int val = Integer.parseInt(getValue().toString());

    return player.getOnlineTime() >= val;
  }
}