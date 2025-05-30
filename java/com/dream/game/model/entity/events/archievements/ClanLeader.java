package com.dream.game.model.entity.events.archievements;

import com.dream.game.model.actor.instance.L2PcInstance;

public class ClanLeader extends Condition
   {
  public ClanLeader(Object value)
    {
    super(value);
    setName("Be Clan Leader");
   }

  @Override
  public boolean meetConditionRequirements(L2PcInstance player)
  {
    if (getValue() == null) {
      return false;
    }

    return (player.getClan() != null) && 
      (player.isClanLeader());
    }
  }