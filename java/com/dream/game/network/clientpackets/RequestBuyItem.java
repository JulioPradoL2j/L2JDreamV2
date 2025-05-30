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
package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.datatables.xml.BuyListTable;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2MerchantInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.buylist.NpcBuyList;
import com.dream.game.model.buylist.Product;
import com.dream.game.model.holders.ItemHolder;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.util.Util;

import java.util.ArrayList;
import java.util.List;

public final class RequestBuyItem extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 8; // length of the one item

	private int _listId;
	private List<ItemHolder> _items = null;

	@Override
	protected void readImpl()
	{
		_listId = readD();
		int count = readD();
		if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
			return;

		_items = new ArrayList<>(count);
		for (int i = 0; i < count; i++)
		{
			int itemId = readD();
			int cnt = readD();

			if (itemId < 1 || cnt < 1)
				return;

			_items.add(new ItemHolder(itemId, cnt));
		}
	}

	@Override
	protected void runImpl()
	{
		if (_items == null)
			return;

		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0)
			return;

		if (player.getSecondRefusal())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2Npc merchant = null;
		if (!player.isGM())
		{
			merchant = player.getTarget() instanceof L2MerchantInstance ? (L2Npc) player.getTarget() : null;
			if (merchant == null || !merchant.canInteract(player))
				return;
		}

		final NpcBuyList buyList = BuyListTable.getInstance().getBuyList(_listId);
		if (buyList == null)
		{
			Util.handleIllegalPlayerAction(player, player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id " + _listId, Config.DEFAULT_PUNISH);
			return;
		}

		double castleTaxRate = 0;

		if (merchant != null)
		{
			if (!buyList.isNpcAllowed(merchant.getNpcId()))
				return;

			if (merchant instanceof L2MerchantInstance)
			{
				castleTaxRate = merchant.getCastle().getTaxRate();
			}
		}

		int subTotal = 0;
		int slots = 0;
		int weight = 0;

		for (ItemHolder i : _items)
		{
			int price = -1;

			final Product product = buyList.getProductByItemId(i.getId());
			if (product == null)
			{
				Util.handleIllegalPlayerAction(player, player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id " + _listId + " and item_id " + i.getId(), Config.DEFAULT_PUNISH);
				return;
			}

			if (!product.getItem().isStackable() && i.getCount() > 1)
			{
				Util.handleIllegalPlayerAction(player, player.getName() + " of account " + player.getAccountName() + " tried to purchase invalid quantity of items at the same time.", Config.DEFAULT_PUNISH);
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
				return;
			}

			price = product.getPrice();
			if (i.getId() >= 3960 && i.getId() <= 4026)
			{
				price *= Config.RATE_SIEGE_GUARDS_PRICE;
			}

			if (price < 0)
				return;

			if (price == 0 && !player.isGM())
			{
				Util.handleIllegalPlayerAction(player, player.getName() + " of account " + player.getAccountName() + " tried buy item for 0 adena.", Config.DEFAULT_PUNISH);
				return;
			}

			if (product.hasLimitedStock())
				// trying to buy more then available
				if (i.getCount() > product.getCount())
					return;

			if (Integer.MAX_VALUE / i.getCount() < price)
			{
				Util.handleIllegalPlayerAction(player, player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " adena worth of goods.", Config.DEFAULT_PUNISH);
				return;
			}

			// first calculate price per item with tax, then multiply by count
			price = (int) (price * (1 + castleTaxRate));
			subTotal += i.getCount() * price;

			if (subTotal > Integer.MAX_VALUE)
			{
				Util.handleIllegalPlayerAction(player, player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " adena worth of goods.", Config.DEFAULT_PUNISH);
				return;
			}

			weight += i.getCount() * product.getItem().getWeight();
			if (!product.getItem().isStackable())
			{
				slots += i.getCount();
			}
			else if (player.getInventory().getItemByItemId(i.getId()) == null)
			{
				slots++;
			}
		}

		if (!player.isGM() && (weight > Integer.MAX_VALUE || weight < 0 || !player.getInventory().validateWeight(weight)))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
			return;
		}

		if (!player.isGM() && (slots > Integer.MAX_VALUE || slots < 0 || !player.getInventory().validateCapacity(slots)))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SLOTS_FULL));
			return;
		}

		// Charge buyer and add tax to castle treasury if not owned by npc clan
		if (subTotal < 0 || !player.reduceAdena("Buy", subTotal, player.getLastFolkNPC(), false))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			return;
		}

		// Proceed the purchase
		for (ItemHolder i : _items)
		{
			final Product product = buyList.getProductByItemId(i.getId());
			if (product == null)
			{
				Util.handleIllegalPlayerAction(player, player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id " + _listId + " and item_id " + i.getId(), Config.DEFAULT_PUNISH);
				continue;
			}

			if (product.hasLimitedStock())
			{
				if (product.decreaseCount(i.getCount()))
				{
					player.getInventory().addItem("Buy", i.getId(), i.getCount(), player, merchant);
				}
			}
			else
			{
				player.getInventory().addItem("Buy", i.getId(), i.getCount(), player, merchant);
			}
		}

		// add to castle treasury
		if (merchant instanceof L2MerchantInstance)
		{
			merchant.getCastle().addToTreasury((int) (subTotal * castleTaxRate));
		}

		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		player.sendPacket(new ItemList(player, true));
	}
}