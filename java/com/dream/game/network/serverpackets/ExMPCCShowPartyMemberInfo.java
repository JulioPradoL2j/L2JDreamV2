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

public class ExMPCCShowPartyMemberInfo extends L2GameServerPacket
{
	private final L2Party _party;

	public ExMPCCShowPartyMemberInfo(L2Party party)
	{
		_party = party;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x4a);

		writeD(_party.getMemberCount());
		for (L2PcInstance mem : _party.getPartyMembers())
		{
			writeS(mem.getName());
			writeD(mem.getObjectId());
			writeD(mem.getClassId().getId());
		}
	}

}