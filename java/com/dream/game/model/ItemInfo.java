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
package com.dream.game.model;

import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.templates.item.L2Item;

public class ItemInfo
{
	private int _objectId;

	private L2Item _item;

	private int _enchant;

	private int _augmentation;

	private int _count;

	private int _price;

	private int _type1;
	private int _type2;

	private int _equipped;

	private int _change;

	private int _mana;

	private int _location;

	public ItemInfo(L2ItemInstance item)
	{
		if (item == null)
			return;

		// Get the Identifier of the L2ItemInstance
		_objectId = item.getObjectId();

		// Get the L2Item of the L2ItemInstance
		_item = item.getItem();

		// Get the enchant level of the L2ItemInstance
		_enchant = item.getEnchantLevel();

		// Get the augmentation boni
		if (item.isAugmented())
		{
			_augmentation = item.getAugmentation().getAugmentationId();
		}
		else
		{
			_augmentation = 0;
		}

		// Get the quantity of the L2ItemInstance
		_count = item.getCount();

		// Get custom item types (used loto, race tickets)
		_type1 = item.getCustomType1();
		_type2 = item.getCustomType2();

		// Verify if the L2ItemInstance is equipped
		_equipped = item.isEquipped() ? 1 : 0;

		// Get the action to do clientside
		switch (item.getLastChange())
		{
			case L2ItemInstance.ADDED:
			{
				_change = 1;
				break;
			}
			case L2ItemInstance.MODIFIED:
			{
				_change = 2;
				break;
			}
			case L2ItemInstance.REMOVED:
			{
				_change = 3;
				break;
			}
		}

		// Get shadow item mana
		_mana = item.getMana();

		_location = item.getLocationSlot();

	}

	public ItemInfo(L2ItemInstance item, int change)
	{
		if (item == null)
			return;

		// Get the Identifier of the L2ItemInstance
		_objectId = item.getObjectId();

		// Get the L2Item of the L2ItemInstance
		_item = item.getItem();

		// Get the enchant level of the L2ItemInstance
		_enchant = item.getEnchantLevel();

		// Get the augmentation boni
		if (item.isAugmented())
		{
			_augmentation = item.getAugmentation().getAugmentationId();
		}
		else
		{
			_augmentation = 0;
		}

		// Get the quantity of the L2ItemInstance
		_count = item.getCount();

		// Get custom item types (used loto, race tickets)
		_type1 = item.getCustomType1();
		_type2 = item.getCustomType2();

		// Verify if the L2ItemInstance is equipped
		_equipped = item.isEquipped() ? 1 : 0;

		// Get the action to do clientside
		_change = change;

		// Get shadow item mana
		_mana = item.getMana();

		_location = item.getLocationSlot();

	}

	public int getAugemtationBonus()
	{
		return _augmentation;
	}

	public int getChange()
	{
		return _change;
	}

	public int getCount()
	{
		return _count;
	}

	public int getCustomType1()
	{
		return _type1;
	}

	public int getCustomType2()
	{
		return _type2;
	}

	public int getEnchant()
	{
		return _enchant;
	}

	public int getEquipped()
	{
		return _equipped;
	}

	public L2Item getItem()
	{
		return _item;
	}

	public int getLocation()
	{
		return _location;
	}

	public int getMana()
	{
		return _mana;
	}

	public int getObjectId()
	{
		return _objectId;
	}

	public int getPrice()
	{
		return _price;
	}

}
