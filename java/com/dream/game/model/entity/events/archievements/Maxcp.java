package com.dream.game.model.entity.events.archievements;

import com.dream.game.model.actor.instance.L2PcInstance;

public class Maxcp extends Condition
{
  public Maxcp(Object value)
  {
    super(value);
    setName("Max CP");
  }

  @Override
public boolean meetConditionRequirements(L2PcInstance player)
  {
    if (getValue() == null) {
      return false;
    }
    int val = Integer.parseInt(getValue().toString());

    return player.getMaxCp() >= val;
  }
}