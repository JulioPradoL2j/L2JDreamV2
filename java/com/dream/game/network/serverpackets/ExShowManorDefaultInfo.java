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

import com.dream.game.model.L2Manor;

public class ExShowManorDefaultInfo extends L2GameServerPacket
{
	private List<Integer> _crops = null;

	public ExShowManorDefaultInfo()
	{
		_crops = L2Manor.getInstance().getAllCrops();
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x1E);
		writeC(0);
		writeD(_crops.size());
		for (int cropId : _crops)
		{
			writeD(cropId);
			writeD(L2Manor.getInstance().getSeedLevelByCrop(cropId));
			writeD(L2Manor.getInstance().getSeedBasicPriceByCrop(cropId));
			writeD(L2Manor.getInstance().getCropBasicPrice(cropId));
			writeC(1);
			writeD(L2Manor.getInstance().getRewardItem(cropId, 1));
			writeC(1);
			writeD(L2Manor.getInstance().getRewardItem(cropId, 2));
		}
	}

}