 package com.dream.game.model.entity.events.archievements;

import com.dream.game.model.actor.instance.L2PcInstance;

public class Marry extends Condition
{
  public Marry(Object value)
  {
    super(value);
    setName("Married");
  }

  @Override
public boolean meetConditionRequirements(L2PcInstance player)
  {
    if (getValue() == null) {
      return false;
    }

		return player.isMaried();
  }
}