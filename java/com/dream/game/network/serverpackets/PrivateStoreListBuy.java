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
import com.dream.game.network.SystemChatChannelId;

public class PrivateStoreListBuy extends L2GameServerPacket
{
	private final int _objId;
	private final int _playerAdena;
	private final L2PcInstance _activeChar;
	private final TradeList.TradeItem[] _items;

	public PrivateStoreListBuy(L2PcInstance player, L2PcInstance storePlayer)
	{
		_objId = storePlayer.getObjectId();
		_activeChar = player;

		if (Config.SELL_BY_ITEM)
		{
			final CreatureSay cs11 = new CreatureSay(0, SystemChatChannelId.Chat_None, "SYS", "Atention the store system is based on " + Config.COIN_TEXT + "!"); //
			_activeChar.sendPacket(cs11);
			_playerAdena = _activeChar.getItemCount(Config.SELL_ITEM, -1);
		}
		else
			_playerAdena = _activeChar.getAdena();

		storePlayer.getSellList().updateItems();
		_items = storePlayer.getBuyList().getAvailableItems(player.getInventory());
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xB8);
		writeD(_objId);
		writeD(_playerAdena);

		writeD(_items.length);

		for (TradeList.TradeItem item : _items)
		{
			writeD(item.getObjectId());
			writeD(item.getItem().getItemDisplayId());
			writeH(item.getEnchant());
			writeD(item.getCount());
			writeD(item.getItem().getReferencePrice());
			writeH(0);

			writeD(item.getItem().getBodyPart());
			writeH(item.getItem().getType2());
			writeD(item.getPrice());
			writeD(item.getCount());
		}
	}

}