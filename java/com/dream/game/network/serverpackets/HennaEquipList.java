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

import com.dream.game.model.actor.instance.L2HennaInstance;
import com.dream.game.model.actor.instance.L2PcInstance;

public class HennaEquipList extends L2GameServerPacket
{
	private final L2PcInstance _player;
	private final L2HennaInstance[] _hennaEquipList;

	public HennaEquipList(L2PcInstance player, L2HennaInstance[] hennaEquipList)
	{
		_player = player;
		_hennaEquipList = hennaEquipList;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xe2);
		writeD(_player.getAdena());
		writeD(3);
		writeD(_hennaEquipList.length);

		for (L2HennaInstance element : _hennaEquipList)
			if (_player.getInventory().getItemByItemId(element.getItemIdDye()) != null)
			{
				writeD(element.getSymbolId());
				writeD(element.getItemIdDye());
				writeD(element.getAmountDyeRequire());
				writeD(element.getPrice());
				writeD(1);
			}
	}

}