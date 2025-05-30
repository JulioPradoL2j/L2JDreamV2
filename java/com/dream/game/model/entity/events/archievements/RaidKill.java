 package com.dream.game.model.entity.events.archievements;

import java.util.Iterator;
import java.util.Map;

import com.dream.game.manager.RaidPointsManager;
import com.dream.game.model.actor.instance.L2PcInstance;

public class RaidKill extends Condition
{
  public RaidKill(Object value)
  {
    super(value);
    setName("Raid Kill");
  }

  @SuppressWarnings({
	"rawtypes",
		"cast"
  })
  @Override
  public boolean meetConditionRequirements(L2PcInstance player)
  {
    if (getValue() == null) {
      return false;
    }
    int val = Integer.parseInt(getValue().toString());
    Map list = RaidPointsManager.getList(player);
    Iterator i$;
    if (list != null)
    {
      for (i$ = list.keySet().iterator(); i$.hasNext(); ) { int bid = ((Integer)i$.next()).intValue();

        if (bid == val)
        {
          if (((Integer)RaidPointsManager.getList(player).get(Integer.valueOf(bid))).intValue() > 0)
            return true;
        }
      }
    }
    return false;
  }
}