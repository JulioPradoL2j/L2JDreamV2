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

import com.dream.game.model.L2Object;
import com.dream.game.model.actor.instance.L2ItemInstance;

public final class SpawnItem extends L2GameServerPacket
{
	private final int _objectId;
	private int _itemId;
	private final int _x, _y, _z;
	private int _stackable, _count;

	public SpawnItem(L2Object obj)
	{
		_objectId = obj.getObjectId();
		_x = obj.getX();
		_y = obj.getY();
		_z = obj.getZ();

		if (obj instanceof L2ItemInstance)
		{
			L2ItemInstance item = (L2ItemInstance) obj;
			_itemId = item.getItemDisplayId();
			_stackable = item.isStackable() ? 0x01 : 0x00;
			_count = item.getCount();
		}
		else
		{
			_itemId = obj.getPoly().getPolyId();
			_stackable = 0;
			_count = 1;
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x0b);
		writeD(_objectId);
		writeD(_itemId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_stackable);
		writeD(_count);
		writeD(0x00);
	}

}