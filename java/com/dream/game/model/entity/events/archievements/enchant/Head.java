package com.dream.game.model.entity.events.archievements.enchant;

import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.events.archievements.Condition;
import com.dream.game.model.itemcontainer.Inventory;

/**
  * @author Matim,Wallister
  * @version v1
  */
 public class Head extends Condition
 {
         public Head(Object value)
         {
                 super(value);
                 setName("Helmets");
         }
 
         @Override
         public boolean meetConditionRequirements(L2PcInstance player)
         {
                 if (getValue() == null)
                 {
                         return false;
                 }
                 
                 int val = Integer.parseInt(getValue().toString());
                 
                 L2ItemInstance armor = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD);
                 
                 if (armor != null)
                 {
                         if (armor.getEnchantLevel() >= val)
                         {
                                 return true;
                         }
                 }
                 return false;
         }
 }