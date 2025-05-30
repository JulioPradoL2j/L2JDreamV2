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

import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.model.multisell.Entry;
import com.dream.game.model.multisell.Ingredient;
import com.dream.game.model.multisell.ListContainer;

public final class MultiSellList extends L2GameServerPacket
{
	protected int _listId, _page, _finished;
	protected ListContainer _list;

	public MultiSellList(ListContainer list, int page, int finished)
	{
		_list = list;
		_listId = list.getListId();
		_page = page;
		_finished = finished;
	}

	@Override
	protected void writeImpl()
	{

		writeC(0xd0);
		writeD(_listId);
		writeD(_page);
		writeD(_finished);
		writeD(0x28);
		writeD(_list == null ? 0 : _list.getEntries().size());

		if (_list != null)
		{
			for (Entry ent : _list.getEntries())
			{
				writeD(ent.getEntryId());
				writeD(0x00);
				writeD(0x00);
				writeC(1);
				writeH(ent.getProducts().size());
				writeH(ent.getIngredients().size());

				for (Ingredient i : ent.getProducts())
				{
					writeH(i.getItemId());
					writeD(ItemTable.getInstance().getTemplate(i.getItemId()).getBodyPart());
					writeH(ItemTable.getInstance().getTemplate(i.getItemId()).getType2());
					writeD(i.getItemCount());
					writeH(i.getEnchantmentLevel());
					writeD(0x00);
					writeD(i.getManaLeft());
				}

				for (Ingredient i : ent.getIngredients())
				{
					int items = i.getItemId();
					int typeE = 65335;
					if (items > 0)
					{
						typeE = ItemTable.getInstance().getTemplate(i.getItemId()).getType2();
					}
					writeH(items);
					writeH(typeE);
					writeD(i.getItemCount());
					writeH(i.getEnchantmentLevel());
					writeD(0x00);
					writeD(i.getManaLeft());
				}
			}
		}
	}

}