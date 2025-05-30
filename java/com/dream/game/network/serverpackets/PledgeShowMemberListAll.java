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

import com.dream.game.model.L2Clan;
import com.dream.game.model.L2ClanMember;
import com.dream.game.model.actor.instance.L2PcInstance;

public class PledgeShowMemberListAll extends L2GameServerPacket
{
	private final L2Clan _clan;
	private final int _pledgeType;
	private final String _pledgeName;

	public PledgeShowMemberListAll(L2Clan clan, int pledgeType)
	{
		_clan = clan;
		_pledgeType = pledgeType;

		if (_pledgeType == 0)
		{
			_pledgeName = clan.getName();
		}
		else if (_clan.getSubPledge(_pledgeType) != null)
		{
			_pledgeName = _clan.getSubPledge(_pledgeType).getName();
		}
		else
		{
			_pledgeName = "";
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x53);

		writeD(_pledgeType == 0 ? 0 : 1);
		writeD(_clan.getClanId());
		writeD(_pledgeType);
		writeS(_pledgeName);
		writeS(_clan.getSubPledgeLeaderName(_pledgeType));

		writeD(_clan.getCrestId());
		writeD(_clan.getLevel());
		writeD(_clan.getHasCastle());
		writeD(_clan.getHasHideout());
		writeD(_clan.getRank());
		writeD(_clan.getReputationScore());
		writeD(0); // 0
		writeD(0); // 0
		writeD(_clan.getAllyId());
		writeS(_clan.getAllyName());
		writeD(_clan.getAllyCrestId());
		writeD(_clan.isAtWar() ? 1 : 0);// new c3
		writeD(_clan.getSubPledgeMembersCount(_pledgeType));

		for (L2ClanMember m : _clan.getMembers())
		{
			if (m.getPledgeType() != _pledgeType)
			{
				continue;
			}

			writeS(m.getName());
			writeD(m.getLevel());
			writeD(m.getClassId());

			L2PcInstance player = m.getPlayerInstance();
			if (player != null)
			{
				writeD(player.getAppearance().getSex() ? 1 : 0); // no visible effect
				writeD(player.getRace().ordinal());// writeD(1);
			}
			else
			{
				writeD(0x01); // no visible effect
				writeD(0x01); // writeD(1);
			}

			writeD(m.isOnline() ? m.getObjectId() : 0);
			writeD(m.getSponsor() != 0 || m.getApprentice() != 0 ? 1 : 0);
		}
	}

}