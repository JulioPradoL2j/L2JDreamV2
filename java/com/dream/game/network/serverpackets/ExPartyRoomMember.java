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

import com.dream.game.datatables.xml.MapRegionTable;
import com.dream.game.model.L2Party;
import com.dream.game.model.L2PartyRoom;
import com.dream.game.model.actor.instance.L2PcInstance;

public class ExPartyRoomMember extends L2GameServerPacket
{
	private final L2PartyRoom _room;
	private final boolean _leader;

	public ExPartyRoomMember(L2PartyRoom room)
	{
		this(room, false);
	}

	public ExPartyRoomMember(L2PartyRoom room, boolean leader)
	{
		_room = room;
		_leader = leader;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x08);

		writeD(_leader ? 1 : 0);
		writeD(_room.getMemberCount());
		L2PcInstance leader = _room.getLeader();
		L2Party party = _room.getParty();
		for (L2PcInstance member : _room.getMembers())
		{
			writeD(member.getObjectId());
			writeS(member.getName());
			writeD(member.getClassId().getId());
			writeD(member.getLevel());
			writeD(MapRegionTable.getInstance().getL2Region(member));
			if (leader == member)
			{
				writeD(0x01);
			}
			else if (party != null && party == member.getParty())
			{
				writeD(0x02);
			}
			else
			{
				writeD(0x00);
			}
		}
	}

}