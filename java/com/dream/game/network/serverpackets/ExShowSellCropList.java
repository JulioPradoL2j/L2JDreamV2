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
import java.util.List;
import java.util.Map;

import com.dream.game.manager.CastleManorManager.CropProcure;
import com.dream.game.model.L2Manor;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;

public class ExShowSellCropList extends L2GameServerPacket
{
	private int _manorId = 1;
	private final Map<Integer, L2ItemInstance> _cropsItems;
	private final Map<Integer, CropProcure> _castleCrops;

	public ExShowSellCropList(L2PcInstance player, int manorId, List<CropProcure> crops)
	{
		_manorId = manorId;
		_castleCrops = new HashMap<>();
		_cropsItems = new HashMap<>();

		List<Integer> allCrops = L2Manor.getInstance().getAllCrops();
		for (int cropId : allCrops)
		{
			L2ItemInstance item = player.getInventory().getItemByItemId(cropId);
			if (item != null)
			{
				_cropsItems.put(cropId, item);
			}
		}

		for (CropProcure crop : crops)
			if (_cropsItems.containsKey(crop.getId()) && crop.getAmount() > 0)
			{
				_castleCrops.put(crop.getId(), crop);
			}
	}

	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0x21);

		writeD(_manorId);
		writeD(_cropsItems.size());

		for (L2ItemInstance item : _cropsItems.values())
		{
			writeD(item.getObjectId());
			writeD(item.getItemDisplayId());
			writeD(L2Manor.getInstance().getSeedLevelByCrop(item.getItemDisplayId()));
			writeC(1);
			writeD(L2Manor.getInstance().getRewardItem(item.getItemDisplayId(), 1));
			writeC(1);
			writeD(L2Manor.getInstance().getRewardItem(item.getItemDisplayId(), 2));

			if (_castleCrops.containsKey(item.getItemId()))
			{
				CropProcure crop = _castleCrops.get(item.getItemId());
				writeD(_manorId);
				writeD(crop.getAmount());
				writeD(crop.getPrice());
				writeC(crop.getReward());
			}
			else
			{
				writeD(0xFFFFFFFF);
				writeD(0);
				writeD(0);
				writeC(0);
			}
			writeD(item.getCount());
		}
	}

}