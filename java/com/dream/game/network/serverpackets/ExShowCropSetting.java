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
import com.dream.game.manager.CastleManorManager.CropProcure;
import com.dream.game.model.L2Manor;
import com.dream.game.model.entity.siege.Castle;

public class ExShowCropSetting extends L2GameServerPacket
{
	private final int _manorId;
	private final int _count;
	private final int[] _cropData;

	public ExShowCropSetting(int manorId)
	{
		_manorId = manorId;
		Castle c = CastleManager.getInstance().getCastleById(_manorId);
		List<Integer> crops = L2Manor.getInstance().getCropsForCastle(_manorId);
		_count = crops.size();
		_cropData = new int[_count * 14];
		int i = 0;
		for (int cr : crops)
		{
			_cropData[i * 14] = cr;
			_cropData[i * 14 + 1] = L2Manor.getInstance().getSeedLevelByCrop(cr);
			_cropData[i * 14 + 2] = L2Manor.getInstance().getRewardItem(cr, 1);
			_cropData[i * 14 + 3] = L2Manor.getInstance().getRewardItem(cr, 2);
			_cropData[i * 14 + 4] = L2Manor.getInstance().getCropPuchaseLimit(cr);
			_cropData[i * 14 + 5] = 0;
			_cropData[i * 14 + 6] = L2Manor.getInstance().getCropBasicPrice(cr) * 60 / 100;
			_cropData[i * 14 + 7] = L2Manor.getInstance().getCropBasicPrice(cr) * 10;
			CropProcure cropPr = c.getCrop(cr, CastleManorManager.PERIOD_CURRENT);
			if (cropPr != null)
			{
				_cropData[i * 14 + 8] = cropPr.getStartAmount();
				_cropData[i * 14 + 9] = cropPr.getPrice();
				_cropData[i * 14 + 10] = cropPr.getReward();
			}
			else
			{
				_cropData[i * 14 + 8] = 0;
				_cropData[i * 14 + 9] = 0;
				_cropData[i * 14 + 10] = 0;
			}
			cropPr = c.getCrop(cr, CastleManorManager.PERIOD_NEXT);
			if (cropPr != null)
			{
				_cropData[i * 14 + 11] = cropPr.getStartAmount();
				_cropData[i * 14 + 12] = cropPr.getPrice();
				_cropData[i * 14 + 13] = cropPr.getReward();
			}
			else
			{
				_cropData[i * 14 + 11] = 0;
				_cropData[i * 14 + 12] = 0;
				_cropData[i * 14 + 13] = 0;
			}
			i++;
		}
	}

	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0x20);

		writeD(_manorId);
		writeD(_count);

		for (int i = 0; i < _count; i++)
		{
			writeD(_cropData[i * 14]);
			writeD(_cropData[i * 14 + 1]);
			writeC(1);
			writeD(_cropData[i * 14 + 2]);
			writeC(1);
			writeD(_cropData[i * 14 + 3]);

			writeD(_cropData[i * 14 + 4]);
			writeD(_cropData[i * 14 + 5]);
			writeD(_cropData[i * 14 + 6]);
			writeD(_cropData[i * 14 + 7]);

			writeD(_cropData[i * 14 + 8]);
			writeD(_cropData[i * 14 + 9]);
			writeC(_cropData[i * 14 + 10]);

			writeD(_cropData[i * 14 + 11]);
			writeD(_cropData[i * 14 + 12]);
			writeC(_cropData[i * 14 + 13]);
		}
	}

}