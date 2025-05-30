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
package com.dream.game.model.itemcontainer;

import com.dream.Config;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2ItemInstance.ItemLocation;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.util.LinkedBunch;

public class PcFreight extends ItemContainer
{
	private final L2PcInstance _owner;
	private int _activeLocationId;
	private int _tempOwnerId = 0;

	public PcFreight(L2PcInstance owner)
	{
		_owner = owner;
	}

	@Override
	protected void addItem(L2ItemInstance item)
	{
		super.addItem(item);
		if (_activeLocationId > 0)
		{
			item.setLocation(item.getLocation(), _activeLocationId);
		}
	}

	public void doQuickRestore(int val)
	{
		_tempOwnerId = val;
		restore();
	}

	public int getActiveLocation()
	{
		return _activeLocationId;
	}

	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.FREIGHT;
	}

	@Override
	public L2ItemInstance getItemByItemId(int itemId)
	{
		for (L2ItemInstance item : _items)
			if (item.getItemId() == itemId && (item.getLocationSlot() == 0 || _activeLocationId == 0 || item.getLocationSlot() == _activeLocationId))
				return item;

		return null;
	}

	@Override
	public L2ItemInstance[] getItems()
	{
		LinkedBunch<L2ItemInstance> list = new LinkedBunch<>();
		for (L2ItemInstance item : _items)
			if (item.getLocationSlot() == 0 || item.getLocationSlot() == _activeLocationId)
			{
				list.add(item);
			}

		return list.moveToArray(new L2ItemInstance[list.size()]);
	}

	@Override
	public L2PcInstance getOwner()
	{
		return _owner;
	}

	@Override
	public int getOwnerId()
	{
		if (_owner == null)
			return _tempOwnerId;
		return super.getOwnerId();
	}

	@Override
	public int getSize()
	{
		int size = 0;
		for (L2ItemInstance item : _items)
			if (item.getLocationSlot() == 0 || _activeLocationId == 0 || item.getLocationSlot() == _activeLocationId)
			{
				size++;
			}
		return size;
	}

	@Override
	public void restore()
	{
		int locationId = _activeLocationId;
		_activeLocationId = 0;
		super.restore();
		_activeLocationId = locationId;
	}

	public void setActiveLocation(int locationId)
	{
		_activeLocationId = locationId;
	}

	@Override
	public boolean validateCapacity(int slots)
	{
		int cap = _owner == null ? Config.FREIGHT_SLOTS : _owner.getFreightLimit();
		return getSize() + slots <= cap;
	}
}