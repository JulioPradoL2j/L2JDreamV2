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

import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;

public class MoveToPawn extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _targetId;
	private final int _distance;
	private final int _x, _y, _z;
	private final L2Character _char;

	public MoveToPawn(L2Character cha, L2Character target, int distance)
	{
		_char = cha;
		_charObjId = cha.getObjectId();
		_targetId = target.getObjectId();
		_distance = distance;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
	}

	@Override
	protected final void writeImpl()
	{
		if (((_char instanceof L2PcInstance)) && (((L2PcInstance) _char).isBuffShop()))
		{
			return;
		}
		writeC(0x60);

		writeD(_charObjId);
		writeD(_targetId);
		writeD(_distance);

		writeD(_x);
		writeD(_y);
		writeD(_z);
	}

}