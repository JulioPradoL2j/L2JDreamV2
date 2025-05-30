package com.dream.game.model.entity.events.archievements;

import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.itemcontainer.Inventory;

public class WeaponEnchant extends Condition
 {
         public WeaponEnchant(Object value)
         {
                 super(value);
                 setName("Weapon Enchant");
         }
 
         @Override
         public boolean meetConditionRequirements(L2PcInstance player)
         {
                 if (getValue() == null)
                 {
                         return false;
                 }
                 
                 int val = Integer.parseInt(getValue().toString());
                 
                L2ItemInstance weapon = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
                 
                 if (weapon != null)
                 {
                         if (weapon.getEnchantLevel() >= val)
                         {
                                 return true;
                         }
                 }
                 return false;
         }
 }