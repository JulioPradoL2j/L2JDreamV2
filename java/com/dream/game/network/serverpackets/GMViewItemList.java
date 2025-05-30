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

import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;

public class GMViewItemList extends L2GameServerPacket
{
	private final L2ItemInstance[] _items;
	private final L2PcInstance _cha;
	private final String _playerName;

	public GMViewItemList(L2PcInstance cha)
	{
		_items = cha.getInventory().getItems();
		_playerName = cha.getName();
		_cha = cha;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x94);
		writeS(_playerName);
		writeD(_cha.getInventoryLimit());
		writeH(0x01);
		writeH(_items.length);

		for (L2ItemInstance temp : _items)
		{
			if (temp == null || temp.getItem() == null)
			{
				continue;
			}

			writeH(temp.getItem().getType1());

			writeD(temp.getObjectId());
			writeD(temp.getItemDisplayId());
			writeD(temp.getCount());
			writeH(temp.getItem().getType2());
			writeH(temp.getCustomType1());
			writeH(temp.isEquipped() ? 0x01 : 0x00);
			writeD(temp.getItem().getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
			if (temp.isAugmented())
			{
				writeD(temp.getAugmentation().getAugmentationId());
			}
			else
			{
				writeD(0x00);
			}
			writeD(temp.getMana());
		}
	}

}