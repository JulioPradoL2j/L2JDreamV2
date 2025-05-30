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

import com.dream.game.model.L2ClanMember;

public class PledgeReceiveMemberInfo extends L2GameServerPacket
{
	private final L2ClanMember _member;

	public PledgeReceiveMemberInfo(L2ClanMember member)
	{
		_member = member;
	}

	@Override
	protected void writeImpl()
	{
		if (_member == null)
			return;

		writeC(0xFE);
		writeH(0x3D);

		writeD(_member.getPledgeType());
		writeS(_member.getName());
		writeS(_member.getTitle());
		writeD(_member.getPledgeRank());

		if (_member.getPledgeType() != 0)
		{
			writeS(_member.getClan().getSubPledge(_member.getPledgeType()).getName());
		}
		else
		{
			writeS(_member.getClan().getName());
		}

		writeS(_member.getApprenticeOrSponsorName());
	}

}