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

import com.dream.Config;
import com.dream.game.model.TradeList;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;

public class PrivateStoreManageListBuy extends L2GameServerPacket
{
	private final int _objId;
	private final int _playerAdena;
	private final L2ItemInstance[] _itemList;
	private final TradeList.TradeItem[] _buyList;

	public PrivateStoreManageListBuy(L2PcInstance player)
	{
		_objId = player.getObjectId();
		if (Config.SELL_BY_ITEM)
		{
			_playerAdena = player.getItemCount(Config.SELL_ITEM, -1);
		}
		else
		{
		_playerAdena = player.getAdena();
		}
		_itemList = player.getInventory().getUniqueItems(false, true);
		_buyList = player.getBuyList().getItems();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xB7);

		writeD(_objId);
		writeD(_playerAdena);

		writeD(_itemList.length);
		for (L2ItemInstance item : _itemList)
		{
			writeD(item.getItemDisplayId());
			writeH(item.getEnchantLevel());
			writeD(item.getCount());
			writeD(item.getReferencePrice());
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getItem().getType2());
		}

		writeD(_buyList.length);
		for (TradeList.TradeItem item : _buyList)
		{
			writeD(item.getItem().getItemDisplayId());
			writeH(item.getEnchant());
			writeD(item.getCount());
			writeD(item.getItem().getReferencePrice());
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getItem().getType2());
			writeD(item.getPrice());
			writeD(item.getItem().getReferencePrice());
		}
	}

}