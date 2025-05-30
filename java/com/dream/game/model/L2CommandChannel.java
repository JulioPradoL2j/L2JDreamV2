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
import java.util.concurrent.CopyOnWriteArrayList;

import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.network.serverpackets.ExCloseMPCC;
import com.dream.game.network.serverpackets.ExMultiPartyCommandChannelInfo;
import com.dream.game.network.serverpackets.ExOpenMPCC;
import com.dream.game.network.serverpackets.L2GameServerPacket;
import com.dream.game.network.serverpackets.SystemMessage;

public class L2CommandChannel
{
	private final List<L2Party> _partys = new CopyOnWriteArrayList<>();
	private L2PcInstance _commandLeader;
	private int _channelLvl;


	public L2CommandChannel(L2PcInstance leader)
	{
		_commandLeader = leader;
		_partys.add(leader.getParty());
		_channelLvl = leader.getParty().getLevel();
		leader.getParty().setCommandChannel(this);
		leader.getParty().broadcastToPartyMembers(SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_FORMED));
		leader.getParty().broadcastToPartyMembers(ExOpenMPCC.STATIC_PACKET);
		leader.getParty().broadcastToPartyMembers(new ExMultiPartyCommandChannelInfo(this));
	}

	public void addParty(L2Party party)
	{

		if (party == null)
		{
			return;
		}

		broadcastToChannelMembers(SystemMessage.sendString(party.getLeader().getName() + "'s party has joined the Command Channel."));

		_partys.add(party);
		if (party.getLevel() > _channelLvl)
		{
			_channelLvl = party.getLevel();
		}
		party.setCommandChannel(this);
		party.broadcastToPartyMembers(SystemMessage.getSystemMessage(SystemMessageId.JOINED_COMMAND_CHANNEL));
		party.broadcastToPartyMembers(ExOpenMPCC.STATIC_PACKET);

		broadcastToChannelMembers(new ExMultiPartyCommandChannelInfo(this));
	}

	public void broadcastCSToChannelMembers(CreatureSay gsp, L2PcInstance broadcaster)
	{
		if (_partys != null)
		{
			for (L2Party party : _partys)
				if (party != null)
				{
					party.broadcastCSToPartyMembers(gsp, broadcaster);
				}
		}
	}

	public void broadcastToChannelMembers(L2GameServerPacket gsp)
	{
		if (_partys != null)
		{
			for (L2Party party : _partys)
				if (party != null)
				{
					party.broadcastToPartyMembers(gsp);
				}
		}
	}

	public void broadcastToChannelMembers(L2PcInstance exclude, L2GameServerPacket gsp)
	{
		if (_partys != null)
		{
			for (L2Party party : _partys)
			{
				party.broadcastToPartyMembers(exclude, gsp);
			}
		}
	}

	public boolean contains(L2Character target)
	{
		if (target.getParty() == null)
			return false;

		return _partys.contains(target.getParty());
	}

	public void disbandChannel()
	{
		for (L2Party party : _partys)
		{
			if (party != null)
				removeParty(party);
		}
		_partys.clear();
	}

	public L2PcInstance getChannelLeader()
	{
		return _commandLeader;
	}

	public int getLevel()
	{
		return _channelLvl;
	}

	public int getMemberCount()
	{
		int count = 0;
		if (_partys != null)
		{
			for (L2Party party : _partys)
				if (party != null)
				{
					count += party.getMemberCount();
				}
		}
		return count;
	}

	public List<L2PcInstance> getMembers()
	{
		List<L2PcInstance> members = new ArrayList<>();
		for (L2Party party : getPartys())
		{
			members.addAll(party.getPartyMembers());
		}
		return members;
	}

	public List<L2Party> getPartys()
	{
		return _partys;
	}
	public boolean meetRaidWarCondition(L2Object obj)
	{
		if (!(obj instanceof L2Boss))
			return false;
		int npcId = ((L2Attackable) obj).getNpcId();
		switch (npcId)
		{
			case 29001:
			case 29006:
			case 29014:
			case 29022:
			case 13099:
				return getMemberCount() > 36;
			case 29020:
				return getMemberCount() > 56;
			case 29019:
				return getMemberCount() > 225;
			case 29028:
				return getMemberCount() > 99;
			case 29045:
				return getMemberCount() > 35;
			default:
				return getMemberCount() > 18;
		}
	}

	public void removeParty(L2Party party)
	{
		if (party == null)
			return;

		_partys.remove(party);
		_channelLvl = 0;

		for (L2Party pty : _partys)
		{
			if (pty.getLevel() > _channelLvl)
				_channelLvl = pty.getLevel();
		}

		party.setCommandChannel(null);
		party.broadcastToPartyMembers(ExCloseMPCC.STATIC_PACKET);

		if (_partys.size() < 2)
		{
			broadcastToChannelMembers(SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_DISBANDED));
			disbandChannel();
		}
	}

	public void setChannelLeader(L2PcInstance leader)
	{
		_commandLeader = leader;
	}

}