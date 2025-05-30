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

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.templates.item.L2Item;

public class WareHouseDepositList extends L2GameServerPacket
{
	public static Logger _log = Logger.getLogger(WareHouseDepositList.class.getName());

	public static final int PRIVATE = 1;
	public static final int CLAN = 2;
	public static final int CASTLE = 3; // not sure
	public static final int FREIGHT = 4; // not sure

	private final L2PcInstance _activeChar;
	private final int _activeCharAdena;
	private final List<L2ItemInstance> _items;
	private final int _whType;

	public WareHouseDepositList(L2PcInstance player, int type)
	{
		_activeChar = player;
		_whType = type;
		_activeCharAdena = _activeChar.getAdena();

		_items = _activeChar.getInventory().getAvailableItems(true);

		if (_whType == PRIVATE)
		{
			for (L2ItemInstance temp : player.getInventory().getItems())
				if (temp != null && !temp.isEquipped() && (temp.isShadowItem() || temp.isAugmented() || !temp.isTradeable()) && temp.getItem().getType2() != L2Item.TYPE2_QUEST)
				{
					_items.add(temp);
				}
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x41);
		writeH(_whType);
		writeD(_activeCharAdena);
		int count = _items.size();

		if (_log.isDebugEnabled() || Config.DEBUG)
		{
			_log.debug("count:" + count);
		}

		writeH(count);

		for (L2ItemInstance item : _items)
		{
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemDisplayId());
			writeD(item.getCount());
			writeH(item.getItem().getType2());
			writeH(item.getCustomType1());
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(item.getCustomType2());
			writeH(0x00); // ? 200
			writeD(item.getObjectId());
			if (item.isAugmented())
			{
				writeD(0x0000FFFF & item.getAugmentation().getAugmentationId());
				writeD(item.getAugmentation().getAugmentationId() >> 16);
			}
			else
			{
				writeQ(0x00);
			}
		}
	}

}