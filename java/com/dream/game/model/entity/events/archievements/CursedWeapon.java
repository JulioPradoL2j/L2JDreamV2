package com.dream.game.model.entity.events.archievements;

import com.dream.game.model.actor.instance.L2PcInstance;

public class CursedWeapon extends Condition
{
  public CursedWeapon(Object value)
  {
    super(value);
    setName("Cursed Weapon");
  }

  @Override
public boolean meetConditionRequirements(L2PcInstance player)
  {
    if (getValue() == null) {
      return false;
    }

		return player.isCursedWeaponEquipped();
  }
}