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
package com.dream.game.model;

import java.util.ArrayList;
import java.util.List;

import com.dream.game.datatables.xml.MapRegionTable;
import com.dream.game.manager.PartyRoomManager;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ExClosePartyRoom;
import com.dream.game.network.serverpackets.ExManagePartyRoomMember;
import com.dream.game.network.serverpackets.ExPartyRoomMember;
import com.dream.game.network.serverpackets.L2GameServerPacket;
import com.dream.game.network.serverpackets.PartyMatchDetail;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.lang.L2Math;

public class L2PartyRoom
{
	public static final int getPartyRoomState(L2PcInstance player)
	{
		L2PartyRoom room = player.getPartyRoom();
		if (room == null)
			return 0;
		if (room.getLeader() == player)
			return 1;
		L2Party party = room.getParty();
		if (party != null && party == player.getParty())
			return 2;
		return 0;
	}

	public static final boolean tryJoin(L2PcInstance activeChar, L2PartyRoom room, boolean checkForParty)
	{
		if (activeChar == null)
			return false;

		if (checkForParty)
			if (activeChar.getPartyRoom() != null || activeChar.getParty() != null)
			{
				activeChar.sendPacket(SystemMessageId.PARTY_ROOM_FORBIDDEN);
				return false;
			}

		if (room == null)
		{
			activeChar.sendPacket(SystemMessageId.PARTY_ROOM_FORBIDDEN);
			return false;
		}
		else if (!room.checkLevel(activeChar.getLevel()))
		{
			activeChar.sendPacket(SystemMessageId.CANT_ENTER_PARTY_ROOM);
			return false;
		}
		else if (room.getMemberCount() >= room.getMaxMembers())
		{
			activeChar.sendPacket(SystemMessageId.PARTY_ROOM_FULL);
			return false;
		}
		room.addMember(activeChar);
		return true;
	}

	private final int _id;
	private final List<L2PcInstance> _members;
	private int _minLevel;
	private int _maxLevel;
	private int _lootDist;
	private int _maxMembers;

	private String _title;

	private L2Party _party;

	public L2PartyRoom(int id, int minLevel, int maxLevel, int maxMembers, int lootDist, String title)
	{
		_id = id;
		setMinLevel(minLevel);
		setMaxLevel(maxLevel);
		setMaxMembers(maxMembers);
		_lootDist = lootDist;
		_title = title;
		_members = new ArrayList<>();
		_party = null;
	}

	public void addMember(L2PcInstance player)
	{
		if (getMembers().contains(player))
			return;

		PartyRoomManager.getInstance().removeFromWaitingList(player);
		broadcastPacket(new ExManagePartyRoomMember(ExManagePartyRoomMember.ADDED, player));
		broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ENTERED_PARTY_ROOM).addPcName(player));
		updateRoomStatus(false);
		getMembers().add(player);
		player.setPartyRoom(this);
		player.sendPacket(new PartyMatchDetail(this));
		player.sendPacket(new ExPartyRoomMember(this, getMemberCount() == 1));
	}

	public void addMembers(L2Party party)
	{
		for (L2PcInstance player : party.getPartyMembersWithoutLeader())
		{
			getMembers().add(player);
			player.setPartyRoom(this);
		}
		updateRoomStatus(true);
	}

	public void broadcastPacket(L2GameServerPacket packet)
	{
		for (L2PcInstance player : getMembers())
		{
			player.sendPacket(packet);
		}
	}

	public void broadcastPacket(L2GameServerPacket toLeader, L2GameServerPacket toMember)
	{
		L2PcInstance leader = getLeader();
		for (L2PcInstance player : getMembers())
			if (player == leader)
			{
				player.sendPacket(toLeader);
			}
			else
			{
				player.sendPacket(toMember);
			}
	}

	public final boolean canJoin(L2PcInstance activeChar)
	{
		return activeChar.getPartyRoom() == null && activeChar.getParty() == null && checkLevel(activeChar.getLevel()) && getMemberCount() < getMaxMembers();
	}

	public boolean checkLevel(boolean restrict, int level)
	{
		if (restrict)
			return checkLevel(level);
		return true;
	}

	public boolean checkLevel(int level)
	{
		return level >= getMinLevel() && level <= getMaxLevel();
	}

	public int getId()
	{
		return _id;
	}

	public L2PcInstance getLeader()
	{
		if (_party == null || _party.getLeader() == null)
			return getMembers().get(0);
		return _party.getLeader();
	}

	public int getLocation()
	{
		return MapRegionTable.getInstance().getL2Region(getLeader());
	}

	public int getLootDist()
	{
		return _lootDist;
	}

	public int getMaxLevel()
	{
		return _maxLevel;
	}

	public int getMaxMembers()
	{
		return _maxMembers;
	}

	public int getMemberCount()
	{
		return getMembers().size();
	}

	public final List<L2PcInstance> getMembers()
	{
		return _members;
	}

	public int getMinLevel()
	{
		return _minLevel;
	}

	public L2Party getParty()
	{
		return _party;
	}

	public String getTitle()
	{
		return _title;
	}

	public void removeMember(L2PcInstance member, boolean oust)
	{
		if (getMemberCount() == 1 || !getMembers().remove(member))
			return;

		member.setPartyRoom(null);
		member.sendPacket(ExClosePartyRoom.STATIC_PACKET);
		if (oust)
		{
			member.sendPacket(SystemMessageId.OUSTED_FROM_PARTY_ROOM);
		}
		else
		{
			member.sendPacket(SystemMessageId.PARTY_ROOM_EXITED);
		}

		if (oust)
		{
			broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_KICKED_FROM_PARTY_ROOM).addPcName(member));
		}
		else
		{
			broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_LEFT_PARTY_ROOM).addPcName(member));
		}
		broadcastPacket(new ExManagePartyRoomMember(ExManagePartyRoomMember.REMOVED, member));
		updateRoomStatus(false);
	}

	public void setLootDist(int lootDist)
	{
		_lootDist = lootDist;
		if (getParty() != null)
		{
			getParty().setLootDistribution(lootDist);
		}
	}

	public void setMaxLevel(int maxLevel)
	{
		_maxLevel = L2Math.limit(1, maxLevel, 85);
	}

	public void setMaxMembers(int maxMembers)
	{
		_maxMembers = L2Math.limit(2, maxMembers, 12);
	}

	public void setMinLevel(int minLevel)
	{
		_minLevel = L2Math.limit(1, minLevel, 85);
	}

	public void setParty(L2Party party)
	{
		_party = party;
		if (party == null)
			return;
		_party.setPartyRoom(this);
		_party.setLootDistribution(getLootDist());
		if (getMemberCount() == 1)
		{
			addMembers(party);
		}
	}

	public void setTitle(String title)
	{
		_title = title;
	}

	public void updateRoomStatus(boolean playerList)
	{
		broadcastPacket(new PartyMatchDetail(this));
		if (playerList)
		{
			broadcastPacket(new ExPartyRoomMember(this, true), new ExPartyRoomMember(this));
		}
	}
}