package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.model.TradeList;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.PrivateStoreManageListBuff;
import com.dream.game.network.serverpackets.PrivateStoreManageListSell;
import com.dream.game.network.serverpackets.PrivateStoreMsgSell;

public final class SetPrivateStoreListSell extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 12; // length of the one item

	private boolean _packageSale;
	private Item[] _items = null;

	@Override
	protected void readImpl()
	{
		_packageSale = (readD() == 1);
		int count = readD();
		if ((count < 1) || (count > Config.MAX_ITEM_IN_PACKET) || ((count * BATCH_LENGTH) != _buf.remaining()))
		{
			return;
		}

		_items = new Item[count];
		for (int i = 0; i < count; i++)
		{
			int itemId = readD();
			long cnt = readD();
			int price = readD();

			if ((itemId < 1) || (cnt < 1) || (price < 0))
			{
				_items = null;
				return;
			}
			_items[i] = new Item(itemId, (int) cnt, price);
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		if (_items == null)
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			if (!player.isBuffShop())
			{
				player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			}
			player.broadcastUserInfo();
			if (player.isBuffShop())
			{
				player.sendPacket(new PrivateStoreManageListBuff(player));
			}
			else
			{
				player.sendPacket(new PrivateStoreManageListSell(player, _packageSale));
			}
			return;
		}

		if ((player.isCastingNow() || player.isCastingSimultaneouslyNow()) || player.isInDuel() || player.isArenaProtection())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			if (player.isBuffShop())
			{
				player.sendPacket(new PrivateStoreManageListBuff(player));
			}
			else
			{
				player.sendPacket(new PrivateStoreManageListSell(player, _packageSale));
			}
			return;
		}

		if (player.isInsideZone(L2Zone.FLAG_NOSTORE))
		{
			player.sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
			if (player.isBuffShop())
			{
				player.sendPacket(new PrivateStoreManageListBuff(player));
			}
			else
			{
				player.sendPacket(new PrivateStoreManageListSell(player, _packageSale));
			}
			return;
		}

		int totalCost = player.getAdena();
		if (player.isBuffShop())
		{
			if (_items.length > player.getPrivateBuffShopLimit())
			{
				player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				player.sendPacket(new PrivateStoreManageListBuff(player));
				return;
			}

			player.clearBuffShopSellList();

			for (Item i : _items)
			{
				if ((Integer.MAX_VALUE / i.getCount()) < i.getPrice())
				{
					player.sendPacket(SystemMessageId.EXCEEDED_THE_MAXIMUM);
					player.sendPacket(new PrivateStoreManageListBuff(player));
					return;
				}

				player.addObjectToBuffShopSellList(i.getId(), (int) i.getPrice());

				totalCost = (int) (totalCost + i.getPrice());
				if (totalCost > Integer.MAX_VALUE)
				{
					player.sendPacket(SystemMessageId.EXCEEDED_THE_MAXIMUM);
					player.sendPacket(new PrivateStoreManageListBuff(player));
					return;
				}
			}

			player.sitDown();
			player.setPrivateStoreType(1);
			player.broadcastUserInfo();
			player.broadcastPacket(new PrivateStoreMsgSell(player));
		}
		else
		{
			if (_items.length > player.getPrivateSellStoreLimit())
			{
				player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				player.sendPacket(new PrivateStoreManageListSell(player, _packageSale));
				return;
			}

			TradeList tradeList = player.getSellList();
			tradeList.clear();
			tradeList.setPackaged(_packageSale);

			for (Item i : _items)
			{
				if (!i.addToTradeList(tradeList))
				{
					player.sendPacket(SystemMessageId.EXCEEDED_THE_MAXIMUM);
					player.sendPacket(new PrivateStoreManageListSell(player, _packageSale));
					return;
				}

				totalCost += i.getPrice();
				if (totalCost > Integer.MAX_VALUE)
				{
					player.sendPacket(SystemMessageId.EXCEEDED_THE_MAXIMUM);
					player.sendPacket(new PrivateStoreManageListSell(player, _packageSale));
					return;
				}
				player.sitDown();
				if (_packageSale)
				{
					player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_PACKAGE_SELL);
				}
				else
				{
					player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_SELL);
				}

				player.broadcastUserInfo();
				player.broadcastPacket(new PrivateStoreMsgSell(player));
			}
		}
	}

	private static class Item
	{
		private final int _itemId, _count, _price;

		public Item(int id, int num, int pri)
		{
			_itemId = id;
			_count = num;
			_price = pri;
		}

		public boolean addToTradeList(TradeList list)
		{
			if ((Integer.MAX_VALUE / getCount()) < _price)
			{
				return false;
			}

			list.addItem(getId(), getCount(), _price);
			return true;
		}

		public int getId()
		{
			return _itemId;
		}

		public int getCount()
		{
			return _count;
		}

		public long getPrice()
		{
			return getCount() * _price;
		}
	}
}