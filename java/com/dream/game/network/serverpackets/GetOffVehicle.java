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

import com.dream.game.model.actor.instance.L2BoatInstance;
import com.dream.game.model.actor.instance.L2PcInstance;

public class GetOffVehicle extends L2GameServerPacket
{
	private final int _x;
	private final int _y;
	private final int _z;
	private final L2PcInstance _activeChar;
	private final L2BoatInstance _boat;

	public GetOffVehicle(L2PcInstance activeChar, L2BoatInstance boat, int x, int y, int z)
	{
		_activeChar = activeChar;
		_boat = boat;
		_x = x;
		_y = y;
		_z = z;

		if (_activeChar != null)
		{
			_activeChar.setInBoat(false);
			_activeChar.setBoat(null);
		}
	}

	@Override
	protected void writeImpl()
	{
		if (_boat == null || _activeChar == null)
			return;

		writeC(0x5d);
		writeD(_activeChar.getObjectId());
		writeD(_boat.getObjectId());
		writeD(_x);
		writeD(_y);
		writeD(_z);

	}

}