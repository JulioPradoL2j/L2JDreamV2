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

public class DropItem extends L2GameServerPacket
{
	private final L2ItemInstance _item;
	private final int _charObjId;

	public DropItem(L2ItemInstance item, int playerObjId)
	{
		_item = item;
		_charObjId = playerObjId;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x0c);
		writeD(_charObjId);
		writeD(_item.getObjectId());
		writeD(_item.getItemId());

		writeD(_item.getX());
		writeD(_item.getY());
		writeD(_item.getZ());

		if (_item.isStackable())
		{
			writeD(0x01);
		}
		else
		{
			writeD(0x00);
		}
		writeD(_item.getCount());

		writeD(1);
	}

}