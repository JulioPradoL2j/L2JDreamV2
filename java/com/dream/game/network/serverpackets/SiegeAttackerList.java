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

import java.util.ArrayList;
import java.util.List;

import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.manager.clanhallsiege.DevastatedCastleSiege;
import com.dream.game.manager.clanhallsiege.FortressOfDeadSiege;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2SiegeClan;
import com.dream.game.model.entity.ClanHall;
import com.dream.game.model.entity.siege.Castle;

public class SiegeAttackerList extends L2GameServerPacket
{
	private final Castle _castle;
	private final ClanHall _clanHall;

	public SiegeAttackerList(Castle castle, ClanHall clanHall)
	{
		_castle = castle;
		_clanHall = clanHall;
	}

	@Override
	protected final void writeImpl()
	{
		List<L2SiegeClan> clans = new ArrayList<>();
		if (_castle == null)
		{
			if (_clanHall.getId() == 34)
			{
				clans = DevastatedCastleSiege.getInstance().getRegisteredClans();
			}
			if (_clanHall.getId() == 64)
			{
				clans = FortressOfDeadSiege.getInstance().getRegisteredClans();
			}
		}
		else
		{
			clans = _castle.getSiege().getAttackerClans();
		}
		writeC(0xca);
		if (_castle == null)
		{
			writeD(_clanHall.getId());
		}
		else
		{
			writeD(_castle.getCastleId());
		}
		writeD(0x00);
		writeD(0x01);
		writeD(0x00);
		int size = clans.size();
		if (size > 0)
		{
			L2Clan clan;
			writeD(size);
			writeD(size);
			for (L2SiegeClan siegeclan : clans)
			{
				clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
				if (clan == null)
				{
					continue;
				}

				writeD(clan.getClanId());
				writeS(clan.getName());
				writeS(clan.getLeaderName());
				writeD(clan.getCrestId());
				writeD(0x00);
				writeD(clan.getAllyId());
				writeS(clan.getAllyName());
				writeS("");
				writeD(clan.getAllyCrestId());
			}
		}
		else
		{
			writeD(0x00);
			writeD(0x00);
		}
	}

}