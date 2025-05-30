/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.dream.game.skills.conditions;

import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.itemcontainer.Inventory;
import com.dream.game.skills.Env;
import com.dream.game.templates.item.L2ArmorType;
import com.dream.game.templates.item.L2Item;

public class ConditionUsingItemType extends Condition
{
	private final int _mask;

	public ConditionUsingItemType(int mask)
	{
		_mask = mask;
	}

	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.player instanceof L2PcInstance))
			return false;
		Inventory inv = ((L2PcInstance) env.player).getInventory();

		if (_mask == L2ArmorType.LIGHT.mask() || _mask == L2ArmorType.HEAVY.mask() || _mask == L2ArmorType.MAGIC.mask())
		{
			L2ItemInstance chest = inv.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
			if (chest == null)
				return false;
			int chestMask = chest.getItem().getItemMask();

			if ((_mask & chestMask) == 0)
				return false;

			int chestBodyPart = chest.getItem().getBodyPart();

			if (chestBodyPart == L2Item.SLOT_FULL_ARMOR)
				return true;

			L2ItemInstance legs = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
			if (legs == null)
				return false;
			int legMask = legs.getItem().getItemMask();

			return (_mask & legMask) != 0;
		}
		return (_mask & inv.getWearedMask()) != 0;
	}
}