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

import com.dream.game.manager.CastleManorManager.SeedProduction;
import com.dream.game.model.L2Manor;

public class ExShowSeedInfo extends L2GameServerPacket
{
	private List<SeedProduction> _seeds;
	private final int _manorId;

	public ExShowSeedInfo(int manorId, List<SeedProduction> seeds)
	{
		_manorId = manorId;
		_seeds = seeds;
		if (_seeds == null)
		{
			_seeds = new ArrayList<>();
		}
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x1C);
		writeC(0);
		writeD(_manorId);
		writeD(0);
		writeD(_seeds.size());
		for (SeedProduction seed : _seeds)
		{
			writeD(seed.getId());
			writeD(seed.getCanProduce());
			writeD(seed.getStartProduce());
			writeD(seed.getPrice());
			writeD(L2Manor.getInstance().getSeedLevel(seed.getId()));
			writeC(1);
			writeD(L2Manor.getInstance().getRewardItemBySeed(seed.getId(), 1));
			writeC(1);
			writeD(L2Manor.getInstance().getRewardItemBySeed(seed.getId(), 2));
		}
	}

}