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

import com.dream.game.manager.CastleManorManager.CropProcure;
import com.dream.game.model.L2Manor;

public class ExShowCropInfo extends L2GameServerPacket
{
	private List<CropProcure> _crops;
	private final int _manorId;

	public ExShowCropInfo(int manorId, List<CropProcure> crops)
	{
		_manorId = manorId;
		_crops = crops;
		if (_crops == null)
		{
			_crops = new ArrayList<>();
		}
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x1D);
		writeC(0);
		writeD(_manorId);
		writeD(0);
		writeD(_crops.size());
		for (CropProcure crop : _crops)
		{
			writeD(crop.getId());
			writeD(crop.getAmount());
			writeD(crop.getStartAmount());
			writeD(crop.getPrice());
			writeC(crop.getReward());
			writeD(L2Manor.getInstance().getSeedLevelByCrop(crop.getId()));
			writeC(1);
			writeD(L2Manor.getInstance().getRewardItem(crop.getId(), 1));
			writeC(1);
			writeD(L2Manor.getInstance().getRewardItem(crop.getId(), 2));
		}
	}

}