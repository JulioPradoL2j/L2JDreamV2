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

import com.dream.game.model.actor.instance.L2PcInstance;

public class MoveOnVehicle extends L2GameServerPacket
{
	private final int _id;
	private final int _x, _y, _z;
	private final L2PcInstance _activeChar;

	public MoveOnVehicle(int vehicleID, L2PcInstance player, int x, int y, int z)
	{
		_id = vehicleID;
		_activeChar = player;
		_x = x;
		_y = y;
		_z = z;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x71);

		writeD(_activeChar.getObjectId());
		writeD(_id);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_activeChar.getX());
		writeD(_activeChar.getY());
		writeD(_activeChar.getZ());
	}

}