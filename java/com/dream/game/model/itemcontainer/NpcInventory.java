/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.dream.game.model.itemcontainer;

import java.util.ArrayList;
import java.util.List;

import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2ItemInstance.ItemLocation;

public class NpcInventory extends Inventory
{
	public static final int ADENA_ID = 57;
	public static final int ANCIENT_ADENA_ID = 5575;

	private final L2Npc _owner;

	public boolean sshotInUse = false;
	public boolean bshotInUse = false;

	public NpcInventory(L2Npc owner)
	{
		_owner = owner;
	}

	public L2ItemInstance[] getAllItemsByItemId(int itemId)
	{
		List<L2ItemInstance> list = new ArrayList<>();
		for (L2ItemInstance item : _items)
			if (item.getItemId() == itemId)
			{
				list.add(item);
			}

		return list.toArray(new L2ItemInstance[list.size()]);
	}

	@Override
	protected ItemLocation getBaseLocation()
	{
		return ItemLocation.NPC;
	}

	@Override
	protected ItemLocation getEquipLocation()
	{
		return ItemLocation.NPC;
	}

	@Override
	public L2Npc getOwner()
	{
		return _owner;
	}

	@Override
	public void refreshWeight()
	{

	}

	public void reset()
	{
		destroyAllItems("Reset", null, null);
		if (_owner.getTemplate().getSS() > 0)
		{
			addItem("Reset", 1835, _owner.getTemplate().getSS(), null, null);
		}
		if (_owner.getTemplate().getBSS() > 0)
		{
			addItem("Reset", 3947, _owner.getTemplate().getBSS(), null, null);
		}
	}

	@Override
	public void restore()
	{

	}

	@Override
	public void updateInventory(L2ItemInstance newItem)
	{

	}
}