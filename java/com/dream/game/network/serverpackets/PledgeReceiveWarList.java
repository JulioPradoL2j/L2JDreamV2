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

import java.util.List;

import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.model.L2Clan;

public class PledgeReceiveWarList extends L2GameServerPacket
{
	private final List<Integer> _clanList;
	private final int _tab;
	private final int _page;

	public PledgeReceiveWarList(List<Integer> clanList, int tab, int page)
	{
		_clanList = clanList;
		_tab = tab;
		_page = page;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x3e);
		writeD(_tab);
		writeD(_page);
		writeD(_tab == 0 ? _clanList.size() : _page == 0 ? _clanList.size() >= 13 ? 13 : _clanList.size() : _clanList.size() % (13 * _page));

		int index = 0;
		for (int clanId : _clanList)
		{
			L2Clan clan = ClanTable.getInstance().getClan(clanId);
			if (clan == null)
			{
				continue;
			}

			if (_tab != 0)
			{
				if (index < _page * 13)
				{
					index++;
					continue;
				}

				if (index == (_page + 1) * 13)
				{
					break;
				}

				index++;
			}

			writeS(clan.getName());
			writeD(_tab);
			writeD(_page);
		}
	}

}