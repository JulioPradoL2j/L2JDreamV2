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
import com.dream.game.model.L2Spawn;
import com.dream.game.model.entity.siege.Fort;

public class ExShowFortressMapInfo extends L2GameServerPacket
{
	private final Fort _fortress;

	public ExShowFortressMapInfo(Fort fortress)
	{
		_fortress = fortress;
	}

	private boolean isSpawned(int npcId)
	{
		boolean ret = false;
		for (L2Spawn spawn : _fortress.getSiege().getCommanders().get(_fortress.getFortId()))
			if (spawn.getNpcid() == npcId)
			{
				ret = true;
			}
		return ret;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x7d);

		writeD(_fortress.getFortId());
		writeD(_fortress.getSiege().getIsInProgress() ? 1 : 0);
		writeD(_fortress.getFortSize());

		List<SiegeSpawn> commanders = FortSiegeManager.getInstance().getCommanderSpawnList(_fortress.getFortId());
		if (commanders != null && !commanders.isEmpty() && _fortress.getSiege().getIsInProgress())
		{
			switch (commanders.size())
			{
				case 3:
				{
					for (SiegeSpawn spawn : commanders)
						if (isSpawned(spawn.getNpcId()))
						{
							writeD(0);
						}
						else
						{
							writeD(1);
						}
					break;
				}
				case 4:
				{
					int count = 0;
					for (SiegeSpawn spawn : commanders)
					{
						count++;
						if (count == 4)
						{
							writeD(1);
						}
						if (isSpawned(spawn.getNpcId()))
						{
							writeD(0);
						}
						else
						{
							writeD(1);
						}
					}
					break;
				}
			}
		}
		else
		{
			for (int i = 0; i < _fortress.getFortSize(); i++)
			{
				writeD(0);
			}
		}
	}

}