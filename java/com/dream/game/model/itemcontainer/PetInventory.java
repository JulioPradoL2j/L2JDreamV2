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

import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2ItemInstance.ItemLocation;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.network.serverpackets.PetInventoryUpdate;
import com.dream.game.templates.item.L2EtcItemType;
import com.dream.game.templates.item.L2Item;

public class PetInventory extends Inventory
{
	private final L2PetInstance _owner;

	public PetInventory(L2PetInstance owner)
	{
		_owner = owner;
	}

	@Override
	protected ItemLocation getBaseLocation()
	{
		return ItemLocation.PET;
	}

	@Override
	protected ItemLocation getEquipLocation()
	{
		return ItemLocation.PET_EQUIP;
	}

	@Override
	public L2PetInstance getOwner()
	{
		return _owner;
	}

	@Override
	public int getOwnerId()
	{
		int id = 0;

		if (_owner != null)
		{
			id = _owner.getOwner().getObjectId();
		}

		return id;
	}

	@Override
	public void refreshWeight()
	{
		super.refreshWeight();
		getOwner().broadcastFullInfo();
	}

	@Override
	public void updateDatabase()
	{

	}

	@Override
	public void updateInventory(L2ItemInstance newItem)
	{
		PetInventoryUpdate petIU = new PetInventoryUpdate();
		petIU.addItem(newItem);
		getOwner().getOwner().sendPacket(petIU);
	}

	@Override
	public boolean validateCapacity(int slots)
	{
		return _items.size() + slots <= _owner.getInventoryLimit();
	}

	public boolean validateCapacity(L2ItemInstance item)
	{
		int slots = 0;

		if (!(item.isStackable() && getItemByItemId(item.getItemId()) != null) && item.getItemType() != L2EtcItemType.HERB)
		{
			slots++;
		}

		return validateCapacity(slots);
	}

	@Override
	public boolean validateWeight(int weight)
	{
		return _totalWeight + weight <= _owner.getMaxLoad();
	}

	public boolean validateWeight(L2ItemInstance item, int count)
	{
		int weight = 0;
		L2Item template = ItemTable.getInstance().getTemplate(item.getItemId());
		if (template == null)
			return false;
		weight += count * template.getWeight();
		return validateWeight(weight);
	}
}