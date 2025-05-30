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
import com.dream.game.model.actor.instance.L2PcInstance;

public class PrivateStoreManageListSell extends L2GameServerPacket
{
	private final int _objId;
	private final int _playerAdena;
	private final boolean _packageSale;
	private final TradeList.TradeItem[] _itemList;
	private final TradeList.TradeItem[] _sellList;

	public PrivateStoreManageListSell(L2PcInstance player, boolean isPackageSale)
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
		player.getSellList().updateItems();
		_packageSale = isPackageSale;
		_itemList = player.getInventory().getAvailableItems(player.getSellList());
		_sellList = player.getSellList().getItems();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x9A);
		writeD(_objId);
		writeD(_packageSale ? 1 : 0);
		writeD(_playerAdena);

		writeD(_itemList.length);
		for (TradeList.TradeItem item : _itemList)
		{
			writeD(item.getItem().getType2());
			writeD(item.getObjectId());
			writeD(item.getItem().getItemDisplayId());
			writeD(item.getCount());
			writeH(0x00);
			writeH(item.getEnchant());
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeD(item.getPrice());
		}

		writeD(_sellList.length);
		for (TradeList.TradeItem item : _sellList)
		{
			writeD(item.getItem().getType2());
			writeD(item.getObjectId());
			writeD(item.getItem().getItemDisplayId());
			writeD(item.getCount());
			writeH(0x00);
			writeH(item.getEnchant());
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeD(item.getPrice());
			writeD(item.getItem().getReferencePrice());
		}
	}

}