package com.dream.game.model.entity.events.archievements;

import com.dream.game.model.actor.instance.L2PcInstance;

public class Vip extends Condition
{
  public Vip(Object value)
  {
    super(value);
    setName("Vip");
  }

  @Override
public boolean meetConditionRequirements(L2PcInstance player)
  {
    if (getValue() == null) {
      return false;
    }

    return player.isVip();
  }
}