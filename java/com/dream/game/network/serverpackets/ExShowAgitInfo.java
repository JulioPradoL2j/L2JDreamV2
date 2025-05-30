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

import java.util.Map;

import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.manager.ClanHallManager;
import com.dream.game.model.entity.ClanHall;

public class ExShowAgitInfo extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x16);
		Map<Integer, ClanHall> clannhalls = ClanHallManager.getInstance().getAllClanHalls();
		writeD(clannhalls.size());
		for (ClanHall ch : clannhalls.values())
		{
			writeD(ch.getId());
			writeS(ch.getOwnerId() <= 0 ? "" : ClanTable.getInstance().getClan(ch.getOwnerId()).getName());
			writeS(ch.getOwnerId() <= 0 ? "" : ClanTable.getInstance().getClan(ch.getOwnerId()).getLeaderName());
			writeD(ch.getGrade() > 0 ? 0x00 : 0x01);
		}
	}

}