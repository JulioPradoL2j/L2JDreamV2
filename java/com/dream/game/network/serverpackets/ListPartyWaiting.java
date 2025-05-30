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

import static com.dream.game.manager.PartyRoomManager.ENTRIES_PER_PAGE;

import java.util.List;

import com.dream.game.manager.PartyRoomManager;
import com.dream.game.model.L2PartyRoom;
import com.dream.game.model.actor.instance.L2PcInstance;

public class ListPartyWaiting extends L2GameServerPacket
{
	private final List<L2PartyRoom> _rooms;
	private final int _offset;
	private final int _last;

	public ListPartyWaiting(L2PcInstance player, int page)
	{
		_rooms = PartyRoomManager.getInstance().getRooms(player);
		_offset = (page - 1) * ENTRIES_PER_PAGE;
		_last = _offset + Math.min(_rooms.size() - _offset, ENTRIES_PER_PAGE);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x96);

		writeD(_rooms.size() / 64 + 1);
		writeD(_last - _offset);
		for (int i = _offset; i < _last; i++)
		{
			L2PartyRoom room = _rooms.get(i);
			writeD(room.getId());
			writeS(room.getTitle());
			writeD(room.getLocation());
			writeD(room.getMinLevel());
			writeD(room.getMaxLevel());
			writeD(room.getMemberCount());
			writeD(room.getMaxMembers());
			writeS(room.getLeader().getName());
		}
	}

}