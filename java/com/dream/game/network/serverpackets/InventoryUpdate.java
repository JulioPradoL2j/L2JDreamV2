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
package com.dream.game.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.dream.game.model.ItemInfo;
import com.dream.game.model.actor.instance.L2ItemInstance;

public class InventoryUpdate extends L2GameServerPacket
{
	private final List<ItemInfo> _items;

	public InventoryUpdate()
	{
		_items = new ArrayList<>();
	}

	public InventoryUpdate(List<ItemInfo> items)
	{
		_items = items;
	}

	public void addEquipItems(L2ItemInstance[] items)
	{
		if (items != null)
		{
			for (L2ItemInstance item : items)
				if (item != null)
				{
					_items.add(new ItemInfo(item, 2));
				}
		}
	}

	public void addItem(L2ItemInstance item)
	{
		if (item != null)
		{
			_items.add(new ItemInfo(item));
		}
	}

	public void addItems(List<L2ItemInstance> items)
	{
		if (items != null)
		{
			for (L2ItemInstance item : items)
				if (item != null)
				{
					_items.add(new ItemInfo(item));
				}
		}
	}

	public void addModifiedItem(L2ItemInstance item)
	{
		if (item != null)
		{
			_items.add(new ItemInfo(item, 2));
		}
	}

	public void addNewItem(L2ItemInstance item)
	{
		if (item != null)
		{
			_items.add(new ItemInfo(item, 1));
		}
	}

	public void addRemovedItem(L2ItemInstance item)
	{
		if (item != null)
		{
			_items.add(new ItemInfo(item, 3));
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x27);
		int count = _items.size();
		writeH(count);
		for (ItemInfo item : _items)
		{
			writeH(item.getChange());
			writeH(item.getItem().getType1());

			writeD(item.getObjectId());
			writeD(item.getItem().getItemDisplayId());
			writeD(item.getCount());
			writeH(item.getItem().getType2());
			writeH(item.getCustomType1());
			writeH(item.getEquipped());
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchant());
			writeH(item.getCustomType2());
			writeD(item.getAugemtationBonus());
			writeD(item.getMana());
		}
	}

}