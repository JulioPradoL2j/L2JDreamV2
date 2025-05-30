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

import com.dream.game.manager.FortSiegeManager;
import com.dream.game.manager.FortSiegeManager.SiegeSpawn;
import com.dream.game.model.entity.siege.Fort;

public class ExShowFortressSiegeInfo extends L2GameServerPacket
{
	private final int _fortId;
	private final int _size;
	private final Fort _fort;

	public ExShowFortressSiegeInfo(Fort fort)
	{
		_fort = fort;
		_fortId = fort.getFortId();
		_size = fort.getFortSize();
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x17);

		writeD(_fortId);
		writeD(_size);
		List<SiegeSpawn> commanders = FortSiegeManager.getInstance().getCommanderSpawnList(_fortId);
		if (commanders != null && !commanders.isEmpty())
		{
			switch (commanders.size())
			{
				case 3:
					switch (_fort.getSiege().getCommanders().get(_fortId).size())
					{
						case 0:
							writeD(0x03);
							break;
						case 1:
							writeD(0x02);
							break;
						case 2:
							writeD(0x01);
							break;
						case 3:
							writeD(0x00);
							break;
					}
					break;
				case 4:
					switch (_fort.getSiege().getCommanders().get(_fortId).size())
					{
						case 0:
							writeD(0x05);
							break;
						case 1:
							writeD(0x04);
							break;
						case 2:
							writeD(0x03);
							break;
						case 3:
							writeD(0x02);
							break;
						case 4:
							writeD(0x01);
							break;
					}
					break;
			}
		}
		else
		{
			for (int i = 0; i < _size; i++)
			{
				writeD(0x00);
			}
		}
	}

}