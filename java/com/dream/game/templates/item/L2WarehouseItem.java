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
package com.dream.game.templates.item;

import com.dream.game.model.actor.instance.L2ItemInstance;

public class L2WarehouseItem
{

	private final L2Item _item;
	private final int _object;
	private final int _count;
	private final int _owner;
	private final int _enchant;
	private final int _grade;
	private boolean _isAugmented;
	private int _augmentationId;
	private final int _manaLeft;
	private final L2ItemInstance _instance;

	private final int _type1;
	private final int _type2;

	public L2WarehouseItem(L2ItemInstance item)
	{
		_instance = item;
		_item = item.getItem();
		_object = item.getObjectId();
		_count = item.getCount();
		_owner = item.getOwnerId();
		_enchant = item.getEnchantLevel();
		_grade = item.getItem().getItemGrade();
		if (item.isAugmented())
		{
			_isAugmented = true;
			_augmentationId = item.getAugmentation().getAugmentationId();
		}
		else
		{
			_isAugmented = false;
		}
		_manaLeft = item.getMana();
		_type1 = item.getCustomType1();
		_type2 = item.getCustomType2();

	}

	public int getAugmentationId()
	{
		return _augmentationId;
	}

	public final int getBodyPart()
	{
		return _item.getBodyPart();
	}

	public final int getCount()
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

	public final int getEnchantLevel()
	{
		return _enchant;
	}

	public L2Item getItem()
	{
		return _item;
	}

	public final int getItemGrade()
	{
		return _grade;
	}

	public final int getItemId()
	{
		return _item.getItemId();
	}

	public final L2ItemInstance getItemInstance()
	{
		return _instance;
	}

	public String getItemName()
	{
		return _item.getName();
	}

	public final AbstractL2ItemType getItemType()
	{
		return _item.getItemType();
	}

	public int getManaLeft()
	{
		return _manaLeft;
	}

	@Deprecated
	public String getName()
	{
		return _item.getName();
	}

	public final int getObjectId()
	{
		return _object;
	}

	public final int getOwnerId()
	{
		return _owner;
	}

	public final int getType1()
	{
		return _item.getType1();
	}

	public final int getType2()
	{
		return _item.getType2();
	}

	public final boolean isArmor()
	{
		return _item instanceof L2Armor;
	}

	public boolean isAugmented()
	{
		return _isAugmented;
	}

	public final boolean isEtcItem()
	{
		return _item instanceof L2EtcItem;
	}

	public final boolean isWeapon()
	{
		return _item instanceof L2Weapon;
	}

	@Override
	public String toString()
	{
		return _item.toString();
	}

}