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

import com.dream.game.model.L2Clan.RankPrivs;
import com.dream.game.model.L2ClanMember;

public class PledgePowerGradeList extends L2GameServerPacket
{
	private final RankPrivs[] _privs;
	private final L2ClanMember[] _members;

	public PledgePowerGradeList(RankPrivs[] privs, L2ClanMember[] members)
	{
		_privs = privs;
		_members = members;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x3b);
		writeD(_privs.length);
		for (RankPrivs _priv : _privs)
		{
			writeD(_priv.getRank());

			int count = 0;
			for (L2ClanMember member : _members)
				if (member.getPledgeRank() == _priv.getRank())
				{
					count++;
				}
			writeD(count);
		}
	}

}