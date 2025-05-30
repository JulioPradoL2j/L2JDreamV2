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

public class Ride extends L2GameServerPacket
{
	public static final int ACTION_MOUNT = 1;
	public static final int ACTION_DISMOUNT = 0;

	private final int _id;
	private final int _bRide;
	private int _rideType;
	private final int _rideClassID;

	public Ride(int id, int action, int rideClassId)
	{
		_id = id; // charobjectID
		_bRide = action; // 1 for mount ; 2 for dismount
		_rideClassID = rideClassId + 1000000; // npcID

		switch (rideClassId)
		{
			case 12526:
			case 12527: // Striders
			case 12528:
				_rideType = 1;
				break;

			case 12621: // Wyvern
				_rideType = 2;
				break;
		}
	}

	public int getMountType()
	{
		return _rideType;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x86);
		writeD(_id);
		writeD(_bRide);
		writeD(_rideType);
		writeD(_rideClassID);
	}

}