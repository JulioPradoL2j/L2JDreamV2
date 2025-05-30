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

import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.Location;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.SystemMessage;

public class CombatFlag
{
	protected L2PcInstance _player = null;
	public int playerId = 0;
	private L2ItemInstance _item = null;

	private final Location _location;
	public L2ItemInstance itemInstance;

	private final int _itemId;

	public CombatFlag(int fort_id, int x, int y, int z, int heading, int item_id)
	{
		_location = new Location(x, y, z, heading);
		_itemId = item_id;
	}

	public boolean activate(L2PcInstance player, L2ItemInstance item)
	{
		if (player.isMounted())
		{
			player.sendPacket(SystemMessageId.NO_CONDITION_TO_EQUIP);
			return false;
		}

		// Player holding it data
		_player = player;
		playerId = _player.getObjectId();
		itemInstance = null;

		// Equip with the weapon
		_item = item;
		_player.getInventory().equipItemAndRecord(_item);
		_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED).addItemName(_item));

		// Refresh inventory
		InventoryUpdate iu = new InventoryUpdate();
		iu.addItem(_item);
		_player.sendPacket(iu);

		// Refresh player stats
		_player.broadcastUserInfo();
		_player.setCombatFlagEquipped(true);

		return true;
	}

	public void dropIt()
	{
		// Reset player stats
		_player.setCombatFlagEquipped(false);
		int slot = _item.getItem().getBodyPart();
		_player.getInventory().unEquipItemInBodySlotAndRecord(slot);
		_player.destroyItem("CombatFlag", _item, null, true);
		_item = null;
		_player.broadcastUserInfo();
		_player = null;
		playerId = 0;
	}

	public synchronized void spawnMe()
	{
		L2ItemInstance i;

		i = ItemTable.createItem("Combat", _itemId, 1, null, null);
		i.spawnMe(_location.getX(), _location.getY(), _location.getZ());
		itemInstance = i;
	}

	public synchronized void unSpawnMe()
	{
		if (_player != null)
		{
			dropIt();
		}

		if (itemInstance != null)
		{
			itemInstance.decayMe();
		}
	}
}