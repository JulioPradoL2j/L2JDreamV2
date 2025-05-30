package com.dream.game.model.entity.events.archievements;

import com.dream.game.model.actor.instance.L2PcInstance;

public class Pk extends Condition
{
  public Pk(Object value)
  {
    super(value);
    setName("PK Count");
  }

  @Override
public boolean meetConditionRequirements(L2PcInstance player)
  {
    if (getValue() == null) {
      return false;
    }
    int val = Integer.parseInt(getValue().toString());

    return player.getPkKills() >= val;
  }
}