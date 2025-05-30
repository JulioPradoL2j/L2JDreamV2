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

import java.util.List;

import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;

public class PackageSendableList extends L2GameServerPacket
{
	private final List<L2ItemInstance> _items;
	private final int _playerObjId;
	private final int _adena;

	public PackageSendableList(L2PcInstance sender, int playerOID)
	{
		_items = sender.getInventory().getAvailableItems(true);
		_playerObjId = playerOID;
		_adena = sender.getAdena();
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xC3);

		writeD(_playerObjId);
		writeD(_adena);
		writeD(_items.size());
		for (L2ItemInstance item : _items)
		{
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemDisplayId());
			writeD(item.getCount());
			writeH(item.getItem().getType2());
			writeH(item.getCustomType1());
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(item.getCustomType2());
			writeH(0x00);
			writeD(item.getObjectId());
		}
	}

}