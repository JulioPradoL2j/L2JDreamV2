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
import com.dream.game.model.actor.position.L2CharPosition;

public class MoveToLocationInVehicle extends L2GameServerPacket
{
	private int _charObjId;
	private int _boatId;
	private L2CharPosition _destination;
	private L2CharPosition _origin;

	public MoveToLocationInVehicle(L2Character actor, L2CharPosition destination, L2CharPosition origin)
	{
		if (!(actor instanceof L2PcInstance))
			return;

		L2PcInstance player = (L2PcInstance) actor;

		if (player.getBoat() == null)
			return;

		_charObjId = player.getObjectId();
		_boatId = player.getBoat().getObjectId();
		_destination = destination;
		_origin = origin;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0x71);
		writeD(_charObjId);
		writeD(_boatId);
		writeD(_destination.x);
		writeD(_destination.y);
		writeD(_destination.z);
		writeD(_origin.x);
		writeD(_origin.y);
		writeD(_origin.z);
	}

}