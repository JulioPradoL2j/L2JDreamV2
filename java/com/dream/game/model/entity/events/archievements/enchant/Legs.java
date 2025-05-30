package com.dream.game.model.entity.events.archievements.enchant;

import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.events.archievements.Condition;
import com.dream.game.model.itemcontainer.Inventory;

public class Legs extends Condition
 {
         public Legs(Object value)
         {
                 super(value);
                 setName("Legs");
         }
 
         @Override
         public boolean meetConditionRequirements(L2PcInstance player)
         {
                 if (getValue() == null)
                 {
                         return false;
                 }
                 
                 int val = Integer.parseInt(getValue().toString());
                 
                 L2ItemInstance armor = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);
                 
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