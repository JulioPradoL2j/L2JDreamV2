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

import com.dream.Config;
import com.dream.game.model.TradeList;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemChatChannelId;

public class PrivateStoreListSell extends L2GameServerPacket
{
	private final int _objId;
	private final int _playerAdena;
	private final boolean _packageSale;
	private final L2PcInstance _activeChar;
	private Map<Integer, int[]> _buffs = new HashMap<>();
	private TradeList.TradeItem[] _items = new TradeList.TradeItem[0];
	private final L2PcInstance _storePlayer;

	public PrivateStoreListSell(L2PcInstance player, L2PcInstance storePlayer)
	{
		_objId = storePlayer.getObjectId();
		_storePlayer = storePlayer;

		_activeChar = player;

		if (_storePlayer.isBuffShop())
		{
			_buffs = _storePlayer.getBuffShopSellList();
			_packageSale = false;
		}
		else
		{
			_items = _storePlayer.getSellList().getItems();
			_packageSale = _storePlayer.getSellList().isPackaged();
		}

		if (Config.SELL_BY_ITEM)
		{
			final CreatureSay cs11 = new CreatureSay(0, SystemChatChannelId.Chat_None, "SYS", "Atention the store system is based on " + Config.COIN_TEXT + "!");//
			_activeChar.sendPacket(cs11);
			_playerAdena = _activeChar.getItemCount(Config.SELL_ITEM, -1);
		}
		else
			_playerAdena = player.getAdena();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x9B);
		writeD(_objId);
		writeD(_packageSale ? 1 : 0);
		writeD(_playerAdena);

		if (_storePlayer.isBuffShop())
		{
			writeD(_buffs.size());
			for (Map.Entry<Integer, int[]> buff : _buffs.entrySet())
			{
				int[] values = buff.getValue();
				writeD(5);
				writeD(values[0]);
				writeD(buff.getKey().intValue());
				writeD(1);
				writeH(0);
				writeH(0);
				writeH(0);
				writeD(0);
				writeD(values[1]);
				writeD(0);
			}
		}
		else
		{
			writeD(_items.length);
			for (TradeList.TradeItem item : _items)
			{
				writeD(item.getItem().getType2());
				writeD(item.getObjectId());
				writeD(item.getItem().getItemId());
				writeD(item.getCount());
				writeH(0x00);
				writeH(item.getEnchant());
				writeH(0x00);
				writeD(item.getItem().getBodyPart());
				writeD(item.getPrice()); // your price
				writeD(item.getItem().getReferencePrice()); // store price
			}
		}
	}

}