package com.dream.game.model.entity.events.archievements;

import com.dream.game.model.actor.instance.L2PcInstance;

public class Maxhp extends Condition
{
  public Maxhp(Object value)
  {
    super(value);
    setName("Max HP");
  }

  @Override
public boolean meetConditionRequirements(L2PcInstance player)
  {
    if (getValue() == null) {
      return false;
    }
    int val = Integer.parseInt(getValue().toString());

    return player.getMaxHp() >= val;
  }
}