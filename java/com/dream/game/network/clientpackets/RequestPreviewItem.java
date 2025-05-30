package com.dream.game.network.clientpackets;

import java.util.HashMap;
import java.util.Map;

import com.dream.Config;
import com.dream.game.datatables.xml.BuyListTable;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2MerchantInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.buylist.NpcBuyList;
import com.dream.game.model.buylist.Product;
import com.dream.game.model.itemcontainer.Inventory;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.ShopPreviewInfo;
import com.dream.game.network.serverpackets.UserInfo;
import com.dream.game.templates.item.L2Item;
import com.dream.game.util.Util;

public final class RequestPreviewItem extends L2GameClientPacket
{
	private class RemoveWearItemsTask implements Runnable
	{
		private final L2PcInstance activeChar;

		protected RemoveWearItemsTask(L2PcInstance player)
		{
			activeChar = player;
		}

		@Override
		public void run()
		{
			try
			{
				activeChar.sendPacket(SystemMessageId.NO_LONGER_TRYING_ON);
				activeChar.sendPacket(new UserInfo(activeChar));
			}
			catch (Exception e)
			{
				_log.warn("", e);
			}
		}
	}

	protected L2PcInstance _activeChar;
	private Map<Integer, Integer> _itemList;
	@SuppressWarnings("unused")
	private int _unk;
	private int _listId;
	private int _count;

	private int[] _items;

	@Override
	protected void readImpl()
	{
		_unk = readD();
		_listId = readD();
		_count = readD();

		if (_count < 0)
		{
			_count = 0;
		}
		else if (_count > 100)
			return; // prevent too long lists

		// Create _items table that will contain all ItemID to Wear
		_items = new int[_count];

		// Fill _items table with all ItemID to Wear
		for (int i = 0; i < _count; i++)
		{
			_items[i] = readD();
		}
	}

	@Override
	protected void runImpl()
	{
		if (_items == null)
			return;

		if (_count < 1 || _listId >= 4000000)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// Get the current player and return if null
		_activeChar = getClient().getActiveChar();
		if (_activeChar == null)
			return;

		// If Alternate rule Karma punishment is set to true, forbid Wear to player with Karma
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && _activeChar.getKarma() > 0)
			return;

		// Check current target of the player and the INTERACTION_DISTANCE
		L2Object target = _activeChar.getTarget();
		if (!_activeChar.isGM() && (target == null || !(target instanceof L2MerchantInstance) || !_activeChar.isInsideRadius(target, L2Npc.INTERACTION_DISTANCE, false, false)))
			return;

		// Get the current merchant targeted by the player
		final L2MerchantInstance merchant = target instanceof L2MerchantInstance ? (L2MerchantInstance) target : null;
		if (merchant == null)
		{
			_log.warn(getClass().getName() + " Null merchant!");
			return;
		}

		final NpcBuyList buyList = BuyListTable.getInstance().getBuyList(_listId);
		if (buyList == null)
		{
			Util.handleIllegalPlayerAction(_activeChar, _activeChar.getName() + " of account " + _activeChar.getAccountName() + " sent a false BuyList list_id " + _listId, Config.DEFAULT_PUNISH);
			return;
		}

		int totalPrice = 0;
		_listId = buyList.getListId();
		_itemList = new HashMap<>();

		for (int i = 0; i < _count; i++)
		{
			int itemId = _items[i];

			final Product product = buyList.getProductByItemId(itemId);
			if (product == null)
			{
				Util.handleIllegalPlayerAction(_activeChar, _activeChar.getName() + " of account " + _activeChar.getAccountName() + " sent a false BuyList list_id " + _listId + " and item_id " + itemId, Config.DEFAULT_PUNISH);
				return;
			}

			final L2Item template = product.getItem();
			if (template == null)
			{
				continue;
			}

			final int slot = Inventory.getPaperdollIndex(template.getBodyPart());
			if (slot < 0)
			{
				continue;
			}

			if (_itemList.containsKey(slot))
			{
				_activeChar.sendPacket(SystemMessageId.CANNOT_TRY_ON_NOW);
				return;
			}
			_itemList.put(slot, itemId);

			totalPrice += Config.WEAR_PRICE;
			if (totalPrice > Integer.MAX_VALUE)
			{
				Util.handleIllegalPlayerAction(_activeChar, _activeChar.getName() + " of account " + _activeChar.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " adena worth of goods.", Config.DEFAULT_PUNISH);
				return;
			}
		}

		// Charge buyer and add tax to castle treasury if not owned by npc clan because a Try On is not Free
		if (totalPrice < 0 || !_activeChar.reduceAdena("Wear", totalPrice, _activeChar.getLastFolkNPC(), true))
		{
			_activeChar.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			return;
		}

		if (!_itemList.isEmpty())
		{
			_activeChar.sendPacket(new ShopPreviewInfo(_itemList));

			// Schedule task
			ThreadPoolManager.getInstance().scheduleGeneral(new RemoveWearItemsTask(_activeChar), Config.WEAR_DELAY * 1000);
		}
	}
}