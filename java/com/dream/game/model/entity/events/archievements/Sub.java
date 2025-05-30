package com.dream.game.model.entity.events.archievements;

import com.dream.game.model.actor.instance.L2PcInstance;

public class Sub extends Condition
{
  public Sub(Object value)
  {
    super(value);
    setName("Subclass Count");
  }

  @Override
public boolean meetConditionRequirements(L2PcInstance player)
  {
    if (getValue() == null) {
      return false;
    }
    int val = Integer.parseInt(getValue().toString());

    return player.getSubClasses().size() >= val;
  }
}