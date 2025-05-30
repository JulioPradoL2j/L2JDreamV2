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

import java.util.Map;

import com.dream.game.model.itemcontainer.Inventory;

/**
 * <font color="red">This packet still need more work. Main items have all been identified.</font><br>
 * <br>
 * Calls the wearlist ("try on" option), and sends items in good paperdoll slot.
 * @author Gnacik, Tk
 */
public class ShopPreviewInfo extends L2GameServerPacket
{
	private final Map<Integer, Integer> _itemlist;

	public ShopPreviewInfo(Map<Integer, Integer> itemlist)
	{
		_itemlist = itemlist;
	}

	private int getFromList(int key)
	{
		return _itemlist.get(key) != null ? _itemlist.get(key) : 0;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xf0);
		writeD(Inventory.PAPERDOLL_TOTALSLOTS);
		// Slots
		writeD(getFromList(Inventory.PAPERDOLL_REAR)); // unverified
		writeD(getFromList(Inventory.PAPERDOLL_LEAR)); // unverified
		writeD(getFromList(Inventory.PAPERDOLL_NECK)); // unverified
		writeD(getFromList(Inventory.PAPERDOLL_RFINGER)); // unverified
		writeD(getFromList(Inventory.PAPERDOLL_LFINGER)); // unverified
		writeD(getFromList(Inventory.PAPERDOLL_HEAD)); // unverified
		writeD(getFromList(Inventory.PAPERDOLL_RHAND)); // good
		writeD(getFromList(Inventory.PAPERDOLL_LHAND)); // good
		writeD(getFromList(Inventory.PAPERDOLL_GLOVES)); // good
		writeD(getFromList(Inventory.PAPERDOLL_CHEST)); // good
		writeD(getFromList(Inventory.PAPERDOLL_LEGS)); // good
		writeD(getFromList(Inventory.PAPERDOLL_FEET)); // good
		writeD(getFromList(Inventory.PAPERDOLL_BACK)); // unverified
		writeD(getFromList(Inventory.PAPERDOLL_FACE)); // unverified
		writeD(getFromList(Inventory.PAPERDOLL_HAIR)); // unverified
		writeD(getFromList(Inventory.PAPERDOLL_HAIRALL)); // unverified
		writeD(getFromList(Inventory.PAPERDOLL_UNDER)); // unverified
	}
}