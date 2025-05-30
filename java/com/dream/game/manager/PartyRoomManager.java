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
package com.dream.game.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.dream.game.datatables.xml.MapRegionTable;
import com.dream.game.model.L2Party;
import com.dream.game.model.L2PartyRoom;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ExClosePartyRoom;

public class PartyRoomManager
{
	private static class SingletonHolder
	{
		protected static final PartyRoomManager _instance = new PartyRoomManager();
	}

	public static final int ENTRIES_PER_PAGE = 64;

	public static final PartyRoomManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private volatile int _nextId;

	private final HashSet<L2PcInstance> _waitingList;

	private final Map<Integer, L2PartyRoom> _rooms;

	public PartyRoomManager()
	{
		_nextId = 1;
		_waitingList = new HashSet<>();
		_rooms = new HashMap<>();
	}

	public void addToWaitingList(L2PcInstance player)
	{
		if (getWaitingList().add(player))
		{
			player.setLookingForParty(true);
			player.broadcastUserInfo();
		}
	}

	public void createRoom(L2PcInstance leader, int minLevel, int maxLevel, int maxMembers, int lootDist, String title)
	{
		L2PartyRoom room = new L2PartyRoom(_nextId++, minLevel, maxLevel, maxMembers, lootDist, title);
		room.addMember(leader);
		leader.setLookingForParty(true);
		leader.broadcastUserInfo();
		room.setParty(leader.getParty());
		getPartyRooms().put(room.getId(), room);
	}

	public final L2PartyRoom getPartyRoom(int roomId)
	{
		return getPartyRooms().get(roomId);
	}

	private final Map<Integer, L2PartyRoom> getPartyRooms()
	{
		return _rooms;
	}

	public List<L2PartyRoom> getRooms(int reqRegion, int curRegion, boolean lvlRestrict, int lvl)
	{
		if (reqRegion == -2)
			return getRoomsNearby(curRegion, lvlRestrict, lvl);

		List<L2PartyRoom> list = new ArrayList<>();
		for (L2PartyRoom room : getPartyRooms().values())
			if (reqRegion > 0 && room.getLocation() != reqRegion)
			{
				continue;
			}
			else if (!room.checkLevel(lvlRestrict, lvl))
			{
				continue;
			}
			else
			{
				list.add(room);
			}
		return list;
	}

	public List<L2PartyRoom> getRooms(L2PcInstance player)
	{
		return getRooms(player.getPartyMatchingRegion(), MapRegionTable.getInstance().getL2Region(player), player.getPartyMatchingLevelRestriction(), player.getLevel());
	}

	public List<L2PartyRoom> getRoomsNearby(int region, boolean lvlRestrict, int lvl)
	{
		List<L2PartyRoom> list = new ArrayList<>();
		for (L2PartyRoom room : getPartyRooms().values())
		{
			if (room.getLocation() != region || !room.checkLevel(lvlRestrict, lvl))
			{
				continue;
			}
			list.add(room);
		}
		return list;
	}

	private final HashSet<L2PcInstance> getWaitingList()
	{
		return _waitingList;
	}

	public List<L2PcInstance> getWaitingList(int minLevel, int maxLevel)
	{
		List<L2PcInstance> list = new ArrayList<>();
		for (L2PcInstance pc : getWaitingList())
			if (pc.getLevel() >= minLevel && pc.getLevel() <= maxLevel)
			{
				list.add(pc);
			}
		return list;
	}

	public void removeFromWaitingList(L2PcInstance player)
	{
		getWaitingList().remove(player);
		player.setLookingForParty(false);
		player.broadcastUserInfo();
	}

	public void removeRoom(int roomId)
	{
		L2PartyRoom room = getPartyRooms().remove(roomId);
		if (room == null)
			return;

		L2Party party = room.getParty();
		if (party != null)
		{
			room.setParty(null);
			party.setPartyRoom(null);
		}

		for (L2PcInstance member : room.getMembers())
		{
			member.setPartyRoom(null);
			member.sendPacket(ExClosePartyRoom.STATIC_PACKET);
			member.sendPacket(SystemMessageId.PARTY_ROOM_DISBANDED);
		}
	}
}