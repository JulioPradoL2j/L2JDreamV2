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

import com.dream.game.model.L2Party;
import com.dream.game.model.actor.instance.L2PcInstance;

public final class PartySmallWindowAll extends L2GameServerPacket
{
	private final L2Party _party;
	private final L2PcInstance _leader;

	public PartySmallWindowAll(L2Party party)
	{
		_party = party;
		_leader = _party.getLeader();
	}

	@Override
	protected void writeImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		writeC(0x4E);
		writeD(_leader.getObjectId());
		writeD(_party.getLootDistribution());
		writeD(_party.getPartyMembers().size() - 1);

		for (L2PcInstance member : _party.getPartyMembers())
			if (member != null && member != activeChar)
			{
				writeD(member.getObjectId());
				writeS(member.getName());

				writeD((int) member.getStatus().getCurrentCp());
				writeD(member.getMaxCp());

				writeD((int) member.getStatus().getCurrentHp());
				writeD(member.getMaxHp());
				writeD((int) member.getStatus().getCurrentMp());
				writeD(member.getMaxMp());
				writeD(member.getLevel());
				writeD(member.getClassId().getId());
				writeD(0x00);
				writeD(member.getRace().ordinal());
			}
	}

}