/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.dream.game.network.serverpackets;

import java.util.Collection;

import com.dream.Config;
import com.dream.game.model.buylist.NpcBuyList;
import com.dream.game.model.buylist.Product;
import com.dream.game.templates.item.L2Item;

public class ShopPreviewList extends L2GameServerPacket
{
	private final int _listId;
	private final int _money;
	private final int _expertise;
	private final Collection<Product> _list;

	public ShopPreviewList(NpcBuyList list, int currentMoney, int expertiseIndex)
	{
		_listId = list.getListId();
		_list = list.getProducts();
		_money = currentMoney;
		_expertise = expertiseIndex;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xef);
		writeC(0xc0); // ?
		writeC(0x13); // ?
		writeC(0x00); // ?
		writeC(0x00); // ?
		writeD(_money); // current money
		writeD(_listId);

		int newlength = 0;
		for (Product product : _list)
			if (product.getItem().getCrystalType() <= _expertise && product.getItem().isEquipable())
			{
				newlength++;
			}
		writeH(newlength);

		for (Product product : _list)
			if (product.getItem().getCrystalType() <= _expertise && product.getItem().isEquipable())
			{
				writeD(product.getItemId());
				writeH(product.getItem().getType2()); // item type2

				if (product.getItem().getType1() != L2Item.TYPE1_ITEM_QUESTITEM_ADENA)
				{
					writeH(product.getItem().getBodyPart()); // slot
				}
				else
				{
					writeH(0x00); // slot
				}

				writeD(Config.WEAR_PRICE);
			}
	}

}