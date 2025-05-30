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

import com.dream.game.manager.CastleManager;
import com.dream.game.manager.CastleManorManager;
import com.dream.game.manager.CastleManorManager.SeedProduction;
import com.dream.game.model.L2Manor;
import com.dream.game.model.entity.siege.Castle;

public class ExShowSeedSetting extends L2GameServerPacket
{
	private final int _manorId;
	private final int _count;
	private final int[] _seedData;

	public ExShowSeedSetting(int manorId)
	{
		_manorId = manorId;
		Castle c = CastleManager.getInstance().getCastleById(_manorId);
		List<Integer> seeds = L2Manor.getInstance().getSeedsForCastle(_manorId);
		_count = seeds.size();
		_seedData = new int[_count * 12];
		int i = 0;
		for (int s : seeds)
		{
			_seedData[i * 12] = s;
			_seedData[i * 12 + 1] = L2Manor.getInstance().getSeedLevel(s);
			_seedData[i * 12 + 2] = L2Manor.getInstance().getRewardItemBySeed(s, 1);
			_seedData[i * 12 + 3] = L2Manor.getInstance().getRewardItemBySeed(s, 2);
			_seedData[i * 12 + 4] = L2Manor.getInstance().getSeedSaleLimit(s);
			_seedData[i * 12 + 5] = L2Manor.getInstance().getSeedBuyPrice(s);
			_seedData[i * 12 + 6] = L2Manor.getInstance().getSeedBasicPrice(s) * 60 / 100;
			_seedData[i * 12 + 7] = L2Manor.getInstance().getSeedBasicPrice(s) * 10;
			SeedProduction seedPr = c.getSeed(s, CastleManorManager.PERIOD_CURRENT);
			if (seedPr != null)
			{
				_seedData[i * 12 + 8] = seedPr.getStartProduce();
				_seedData[i * 12 + 9] = seedPr.getPrice();
			}
			else
			{
				_seedData[i * 12 + 8] = 0;
				_seedData[i * 12 + 9] = 0;
			}
			seedPr = c.getSeed(s, CastleManorManager.PERIOD_NEXT);
			if (seedPr != null)
			{
				_seedData[i * 12 + 10] = seedPr.getStartProduce();
				_seedData[i * 12 + 11] = seedPr.getPrice();
			}
			else
			{
				_seedData[i * 12 + 10] = 0;
				_seedData[i * 12 + 11] = 0;
			}
			i++;
		}
	}

	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0x1F);

		writeD(_manorId);
		writeD(_count);

		for (int i = 0; i < _count; i++)
		{
			writeD(_seedData[i * 12]);
			writeD(_seedData[i * 12 + 1]);
			writeC(1);
			writeD(_seedData[i * 12 + 2]);
			writeC(1);
			writeD(_seedData[i * 12 + 3]);

			writeD(_seedData[i * 12 + 4]);
			writeD(_seedData[i * 12 + 5]);
			writeD(_seedData[i * 12 + 6]);
			writeD(_seedData[i * 12 + 7]);

			writeD(_seedData[i * 12 + 8]);
			writeD(_seedData[i * 12 + 9]);
			writeD(_seedData[i * 12 + 10]);
			writeD(_seedData[i * 12 + 11]);
		}
	}

}