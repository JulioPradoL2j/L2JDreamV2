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

import java.util.HashMap;
import java.util.Map;

import com.dream.game.manager.CastleManager;
import com.dream.game.manager.CastleManorManager;
import com.dream.game.manager.CastleManorManager.CropProcure;
import com.dream.game.model.entity.siege.Castle;

public class ExShowProcureCropDetail extends L2GameServerPacket
{
	private final int _cropId;
	private final Map<Integer, CropProcure> _castleCrops;

	public ExShowProcureCropDetail(int cropId)
	{
		_cropId = cropId;
		_castleCrops = new HashMap<>();

		for (Castle c : CastleManager.getInstance().getCastles().values())
		{
			CropProcure cropItem = c.getCrop(_cropId, CastleManorManager.PERIOD_CURRENT);
			if (cropItem != null && cropItem.getAmount() > 0)
			{
				_castleCrops.put(c.getCastleId(), cropItem);
			}
		}
	}

	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0x22);

		writeD(_cropId);
		writeD(_castleCrops.size());

		for (int manorId : _castleCrops.keySet())
		{
			CropProcure crop = _castleCrops.get(manorId);
			writeD(manorId);
			writeD(crop.getAmount());
			writeD(crop.getPrice());
			writeC(crop.getReward());
		}
	}

}