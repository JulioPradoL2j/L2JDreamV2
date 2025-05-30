package com.dream.game.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.itemcontainer.PcInventory;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.item.L2EtcItemType;
import com.dream.game.templates.item.L2Item;
import com.dream.util.LinkedBunch;

public class TradeList
{
	public class TradeItem
	{
		private int _objectId;
		private final L2Item _item;
		private int _enchant;
		private int _count;
		private int _price;

		private final L2Augmentation _augmentation = null;

		public TradeItem(L2Item item, int count, int price)
		{
			_objectId = 0;
			_item = item;
			_enchant = 0;
			_count = count;
			_price = price;
		}

		public TradeItem(L2ItemInstance item, int count, int price)
		{
			_objectId = item.getObjectId();
			_item = item.getItem();
			_enchant = item.getEnchantLevel();
			_count = count;
			_price = price;
		}

		public TradeItem(TradeItem item, int count, int price)
		{
			_objectId = item.getObjectId();
			_item = item.getItem();
			_enchant = item.getEnchant();
			_count = count;
			_price = price;
		}

		public int getCount()
		{
			return _count;
		}

		public int getEnchant()
		{
			return _enchant;
		}

		public L2Item getItem()
		{
			return _item;
		}

		public int getObjectId()
		{
			return _objectId;
		}

		public int getPrice()
		{
			return _price;
		}

		public boolean isAugmented()
		{
			return _augmentation == null ? false : true;
		}

		public void setCount(int count)
		{
			_count = count;
		}

		public void setEnchant(int enchant)
		{
			_enchant = enchant;
		}

		public void setObjectId(int objectId)
		{
			_objectId = objectId;
		}

		public void setPrice(int price)
		{
			_price = price;
		}

	}

	private final static Logger _log = Logger.getLogger(TradeList.class.getName());

	private final L2PcInstance _owner;
	private L2PcInstance _partner;
	private final List<TradeItem> _items;
	private String _title;
	private boolean _packaged;
	private boolean _confirmed = false;
	private boolean _locked = false;

	private InventoryUpdate ownerIU;

	private InventoryUpdate partnerIU;

	private L2ItemInstance oldItem;

	private L2ItemInstance newItem;

	public TradeList(L2PcInstance owner)
	{
		_items = new ArrayList<>();
		_owner = owner;
	}

	public synchronized TradeItem addItem(int objectId, int count)
	{
		return addItem(objectId, count, 0);
	}

	public synchronized TradeItem addItem(int objectId, int count, int price)
	{
		if (isLocked())
		{
			_log.warn(_owner.getName() + ": Attempt to modify locked TradeList!");
			return null;
		}
		if (count < 1)
			return null;

		L2ItemInstance item = _owner.getInventory().getItemByObjectId(objectId);
		if (item == null)
		{
			_log.warn(_owner.getName() + ": Attempt to add invalid item to TradeList!");
			return null;
		}
		if (!item.isTradeable() && !_owner.isGM() || item.getItemType() == L2EtcItemType.QUEST)
			return null;

		if (Config.ALT_STRICT_HERO_SYSTEM && item.isHeroItem())
			return null;

		if (count > item.getCount())
			return null;

		if (!item.isStackable() && count > 1)
		{
			_log.warn(_owner.getName() + ": Attempt to add non-stackable item to TradeList with count > 1!");
			return null;
		}
		for (TradeItem checkitem : _items)
			if (checkitem.getObjectId() == objectId)
				return null;
		TradeItem titem = new TradeItem(item, count, price);
		_items.add(titem);

		invalidateConfirmation();
		return titem;
	}

	public synchronized TradeItem addItemByItemId(int itemId, int count, int price)
	{
		if (isLocked())
		{
			_log.warn(_owner.getName() + ": Attempt to modify locked TradeList!");
			return null;
		}

		L2Item item = ItemTable.getInstance().getTemplate(itemId);
		if (item == null)
		{
			_log.warn(_owner.getName() + ": Attempt to add invalid item to TradeList!");
			return null;
		}

		if (Config.ALT_STRICT_HERO_SYSTEM)
			if (item.isHeroItem())
				return null;

		if (!item.isTradeable() || item.getItemType() == L2EtcItemType.QUEST)
			return null;

		if (!item.isStackable() && count > 1)
		{
			_log.warn(_owner.getName() + ": Attempt to add non-stackable item to TradeList with count > 1!");
			return null;
		}

		TradeItem titem = new TradeItem(item, count, price);
		_items.add(titem);

		invalidateConfirmation();
		return titem;
	}

	public TradeItem adjustAvailableItem(L2ItemInstance item)
	{
		if (item.isStackable())
		{
			for (TradeItem exclItem : _items)
				if (exclItem.getItem().getItemId() == item.getItemId())
				{
					if (item.getCount() <= exclItem.getCount())
						return null;

					return new TradeItem(item, item.getCount() - exclItem.getCount(), item.getReferencePrice());
				}
		}
		return new TradeItem(item, item.getCount(), item.getReferencePrice());
	}

	public void adjustItemRequest(ItemRequest item)
	{
		for (TradeItem filtItem : _items)
			if (filtItem.getObjectId() == item.getObjectId())
			{
				if (filtItem.getCount() < item.getCount())
				{
					item.setCount(filtItem.getCount());
				}
				return;
			}
		item.setCount(0);
	}

	public void adjustItemRequestByItemId(ItemRequest item)
	{
		for (TradeItem filtItem : _items)
			if (filtItem.getItem().getItemId() == item.getItemId())
			{
				if (filtItem.getCount() < item.getCount())
				{
					item.setCount(filtItem.getCount());
				}
				return;
			}
		item.setCount(0);
	}

	public int calcItemsWeight()
	{
		int weight = 0;

		for (TradeItem item : _items)
		{
			if (item == null)
			{
				continue;
			}
			L2Item template = ItemTable.getInstance().getTemplate(item.getItem().getItemId());
			if (template == null)
			{
				continue;
			}
			weight += item.getCount() * template.getWeight();
		}

		return weight;
	}

	public void clear()
	{
		_items.clear();
		_locked = false;
	}

	public boolean confirm()
	{
		if (_confirmed)
			return true;

		if (_partner != null)
		{
			TradeList partnerList = _partner.getActiveTradeList();
			if (partnerList == null)
			{
				_log.warn(_partner.getName() + ": Trading partner (" + _partner.getName() + ") is invalid in this trade!");
				return false;
			}

			TradeList sync1, sync2;
			if (getOwner().getObjectId() > partnerList.getOwner().getObjectId())
			{
				sync1 = partnerList;
				sync2 = this;
			}
			else
			{
				sync1 = this;
				sync2 = partnerList;
			}

			synchronized (sync1)
			{
				synchronized (sync2)
				{
					_confirmed = true;
					if (partnerList.isConfirmed())
					{
						partnerList.lock();
						lock();
						if (!partnerList.validate())
							return false;
						if (!validate())
							return false;

						doExchange(partnerList);
					}
					else
					{
						_partner.onTradeConfirm(_owner);
					}
				}
			}
		}
		else
		{
			_confirmed = true;
		}

		return _confirmed;
	}

	public int countItemsSlots(L2PcInstance partner)
	{
		int slots = 0;

		for (TradeItem item : _items)
		{
			if (item == null)
			{
				continue;
			}
			L2Item template = ItemTable.getInstance().getTemplate(item.getItem().getItemId());
			if (template == null)
			{
				continue;
			}
			if (!template.isStackable())
			{
				slots += item.getCount();
			}
			else if (partner.getInventory().getItemByItemId(item.getItem().getItemId()) == null)
			{
				slots++;
			}
		}

		return slots;
	}

	private void doExchange(TradeList partnerList)
	{
		boolean success = false;
		if (!getOwner().getInventory().validateWeight(partnerList.calcItemsWeight()) || !partnerList.getOwner().getInventory().validateWeight(calcItemsWeight()))
		{
			partnerList.getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
			getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
		}
		else if (!getOwner().getInventory().validateCapacity(partnerList.countItemsSlots(getOwner())) || !partnerList.getOwner().getInventory().validateCapacity(countItemsSlots(partnerList.getOwner())))
		{
			partnerList.getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SLOTS_FULL));
			getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SLOTS_FULL));
		}
		else
		{
			ownerIU = new InventoryUpdate();
			partnerIU = new InventoryUpdate();

			partnerList.TransferItems(getOwner(), partnerIU, ownerIU);
			TransferItems(partnerList.getOwner(), ownerIU, partnerIU);

			if (ownerIU != null)
			{
				_owner.sendPacket(ownerIU);
			}
			else
			{
				_owner.sendPacket(new ItemList(_owner, false));
			}

			if (partnerIU != null)
			{
				_partner.sendPacket(partnerIU);
			}
			else
			{
				_partner.sendPacket(new ItemList(_partner, false));
			}

			StatusUpdate playerSU = new StatusUpdate(_owner);
			playerSU.addAttribute(StatusUpdate.CUR_LOAD, _owner.getCurrentLoad());
			_owner.sendPacket(playerSU);
			playerSU = null;

			playerSU = new StatusUpdate(_partner);
			playerSU.addAttribute(StatusUpdate.CUR_LOAD, _partner.getCurrentLoad());
			_partner.sendPacket(playerSU);
			playerSU = null;

			success = true;
		}

		partnerList.getOwner().onTradeFinish(success);
		getOwner().onTradeFinish(success);
	}

	public TradeList.TradeItem[] getAvailableItems(PcInventory inventory)
	{
		LinkedBunch<TradeList.TradeItem> list = new LinkedBunch<>();
		for (TradeList.TradeItem item : _items)
		{
			item = new TradeItem(item, item.getCount(), item.getPrice());
			inventory.adjustAvailableItem(item);
			list.add(item);
		}

		return list.moveToArray(new TradeList.TradeItem[list.size()]);
	}

	public TradeItem getItem(int objectId)
	{
		for (TradeItem item : _items)
			if (item.getObjectId() == objectId)
				return item;
		return null;
	}

	public int getItemCount()
	{
		return _items.size();
	}

	public TradeItem[] getItems()
	{
		return _items.toArray(new TradeItem[_items.size()]);
	}

	public L2PcInstance getOwner()
	{
		return _owner;
	}

	public L2PcInstance getPartner()
	{
		return _partner;
	}

	public String getTitle()
	{
		return _title;
	}

	public void invalidateConfirmation()
	{
		_confirmed = false;
	}

	public boolean isConfirmed()
	{
		return _confirmed;
	}

	public boolean isLocked()
	{
		return _locked;
	}

	public boolean isPackaged()
	{
		return _packaged;
	}

	public void lock()
	{
		_locked = true;
	}

	public synchronized boolean privateStoreBuy(L2PcInstance player, ItemRequest[] items, int price)
	{
		if (_locked)
			return false;
		if (!validate())
		{
			lock();
			return false;
		}

		int slots = 0;
		int weight = 0;

		for (ItemRequest item : items)
		{
			if (item == null)
			{
				continue;
			}
			L2Item template = ItemTable.getInstance().getTemplate(item.getItemId());
			if (template == null)
			{
				continue;
			}
			weight += item.getCount() * template.getWeight();
			if (!template.isStackable())
			{
				slots += item.getCount();
			}
			else if (player.getInventory().getItemByItemId(item.getItemId()) == null)
			{
				slots++;
			}
		}

		if (!player.getInventory().validateWeight(weight))
		{
			player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
			return false;
		}

		if (!player.getInventory().validateCapacity(slots))
		{
			player.sendPacket(SystemMessageId.SLOTS_FULL);
			return false;
		}

		PcInventory ownerInventory = _owner.getInventory();
		PcInventory playerInventory = player.getInventory();

		InventoryUpdate ownerIU = new InventoryUpdate();
		InventoryUpdate playerIU = new InventoryUpdate();

		if (Config.SELL_BY_ITEM)
		{
			if (price > playerInventory.getInventoryItemCount(Config.SELL_ITEM, -1))
			{
				return false;
			}

			final L2ItemInstance item = playerInventory.getItemByItemId(Config.SELL_ITEM);

			if (item == null)
			{
				return false;
			}

			final L2ItemInstance oldItem = player.checkItemManipulation(item.getObjectId(), price, "sell");
			if (oldItem == null)
			{
				return false;
			}

			final L2ItemInstance newItem = playerInventory.transferItem("PrivateStore", item.getObjectId(), price, ownerInventory, player, _owner);
			if (newItem == null)
			{
				return false;
			}

			if (oldItem.getCount() > 0 && oldItem != newItem)
			{
				playerIU.addModifiedItem(oldItem);
			}
			else
			{
				playerIU.addRemovedItem(oldItem);
			}

			if (newItem.getCount() > item.getCount())
			{
				ownerIU.addModifiedItem(newItem);
			}
			else
			{
				ownerIU.addNewItem(newItem);
			}
			_owner.sendPacket(SystemMessage.sendString("You obtained " + price + " " + item.getItemName()));
			player.sendPacket(SystemMessage.sendString("You spent " + price + " " + item.getItemName()));
		}
		else
		{
		if (price > playerInventory.getAdena())
		{
			lock();
			return false;
		}

		L2ItemInstance adenaItem = playerInventory.getAdenaInstance();
		playerInventory.reduceAdena("PrivateStore", price, player, _owner);
		playerIU.addItem(adenaItem);
		ownerInventory.addAdena("PrivateStore", price, _owner, player);
		ownerIU.addItem(ownerInventory.getAdenaInstance());
		}

		for (ItemRequest item : items)
		{
			adjustItemRequest(item);
			if (item.getCount() == 0)
			{
				continue;
			}

			L2ItemInstance oldItem = _owner.checkItemManipulation(item.getObjectId(), item.getCount(), "sell");
			if (oldItem == null)
			{
				lock();
				return false;
			}

			L2ItemInstance newItem = ownerInventory.transferItem("PrivateStore", item.getObjectId(), item.getCount(), playerInventory, _owner, player);
			if (newItem == null)
				return false;
			removeItem(item.getObjectId(), -1, item.getCount());

			if (oldItem.getCount() > 0 && oldItem != newItem)
			{
				ownerIU.addModifiedItem(oldItem);
			}
			else
			{
				ownerIU.addRemovedItem(oldItem);
			}
			if (newItem.getCount() > item.getCount())
			{
				playerIU.addModifiedItem(newItem);
			}
			else
			{
				playerIU.addNewItem(newItem);
			}

			if (newItem.isStackable())
			{
				_owner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PURCHASED_S3_S2_S).addString(player.getName()).addItemName(newItem).addNumber(item.getCount()));
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PURCHASED_S3_S2_S_FROM_S1).addString(_owner.getName()).addItemName(newItem).addNumber(item.getCount()));
			}
			else
			{
				_owner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PURCHASED_S2).addString(player.getName()).addItemName(newItem));
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PURCHASED_S2_FROM_S1).addString(_owner.getName()).addItemName(newItem));
			}
		}

		_owner.sendPacket(ownerIU);
		player.sendPacket(playerIU);
		return true;
	}

	public synchronized boolean privateStoreSell(L2PcInstance player, ItemRequest[] items)
	{
		if (_locked)
			return false;

		PcInventory ownerInventory = _owner.getInventory();
		PcInventory playerInventory = player.getInventory();

		for (ItemRequest item : items)
		{
			L2ItemInstance oldItem = player.checkItemManipulation(item.getObjectId(), item.getCount(), "sell");
			if (oldItem == null)
				return false;
			if (oldItem.getAugmentation() != null)
			{
				String msg = "Transaction failed. Augmented items may not be exchanged.";
				_owner.sendMessage(msg);
				player.sendMessage(msg);
				return false;
			}
		}

		InventoryUpdate ownerIU = new InventoryUpdate();
		InventoryUpdate playerIU = new InventoryUpdate();

		int totalprice = 0;
		boolean badCondition = false;

		for (ItemRequest item : items)
		{
			adjustItemRequestByItemId(item);
			if (item.getCount() == 0)
			{
				continue;
			}

			int price = -1;

			for (TradeItem ti : _items)
				if (ti.getItem().getItemId() == item.getItemId())
				{
					if (ti.getPrice() == item.getPrice())
					{
						price = ti.getPrice();
					}
					break;
				}
			if (price == -1)
			{
				continue;
			}

			oldItem = player.checkItemManipulation(item.getObjectId(), item.getCount(), "sell");
			if (oldItem == null)
			{
				badCondition = true;
			}

			newItem = playerInventory.transferItem("PrivateStore", item.getObjectId(), item.getCount(), ownerInventory, player, _owner);
			if (newItem == null)
			{
				badCondition = true;
			}

			if (badCondition)
			{
				if (totalprice > ownerInventory.getAdena())
					return false;

				L2ItemInstance adenaItem = ownerInventory.getAdenaInstance();
				ownerInventory.reduceAdena("PrivateStore", totalprice, _owner, player);
				ownerIU.addItem(adenaItem);
				playerInventory.addAdena("PrivateStore", totalprice, player, _owner);
				playerIU.addItem(playerInventory.getAdenaInstance());
				return false;
			}

			totalprice += price * item.getCount();
			removeItem(-1, item.getItemId(), item.getCount());

			if (oldItem.getCount() > 0 && oldItem != newItem)
			{
				playerIU.addModifiedItem(oldItem);
			}
			else
			{
				playerIU.addRemovedItem(oldItem);
			}
			if (newItem.getCount() > item.getCount())
			{
				ownerIU.addModifiedItem(newItem);
			}
			else
			{
				ownerIU.addNewItem(newItem);
			}

			if (newItem.isStackable())
			{
				_owner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PURCHASED_S3_S2_S_FROM_S1).addString(player.getName()).addItemName(newItem).addNumber(item.getCount()));
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PURCHASED_S3_S2_S).addString(_owner.getName()).addItemName(newItem).addNumber(item.getCount()));
			}
			else
			{
				_owner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PURCHASED_S2_FROM_S1).addString(player.getName()).addItemName(newItem));
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PURCHASED_S2).addString(_owner.getName()).addItemName(newItem));
			}
		}

		int price = -1;
		if (Config.SELL_BY_ITEM)
		{
			if (price > ownerInventory.getInventoryItemCount(Config.SELL_ITEM, -1))
			{
				lock();
				return false;
			}

			final L2ItemInstance item = ownerInventory.getItemByItemId(Config.SELL_ITEM);
			if (item == null)
			{

				lock();
				return false;
			}

			final L2ItemInstance oldItem = _owner.checkItemManipulation(item.getObjectId(), price, "sell");
			if (oldItem == null)
			{
				lock();
				return false;
			}

			final L2ItemInstance newItem = ownerInventory.transferItem("PrivateStore", item.getObjectId(), price, playerInventory, _owner, player);
			if (newItem == null)
			{
				return false;
			}

			if (oldItem.getCount() > 0 && oldItem != newItem)
			{
				ownerIU.addModifiedItem(oldItem);
			}
			else
			{
				ownerIU.addRemovedItem(oldItem);
			}

			if (newItem.getCount() > item.getCount())
			{
				playerIU.addModifiedItem(newItem);
			}
			else
			{
				playerIU.addNewItem(newItem);
			}
			player.sendPacket(SystemMessage.sendString("You obtained " + price + " " + item.getItemName()));
			_owner.sendPacket(SystemMessage.sendString("You spent " + price + " " + item.getItemName()));
		}
		else
		{
		if (totalprice > ownerInventory.getAdena())
			return false;

		L2ItemInstance adenaItem = ownerInventory.getAdenaInstance();
		ownerInventory.reduceAdena("PrivateStore", totalprice, _owner, player);
		ownerIU.addItem(adenaItem);
		playerInventory.addAdena("PrivateStore", totalprice, player, _owner);
		playerIU.addItem(playerInventory.getAdenaInstance());
		}

		_owner.sendPacket(ownerIU);
		player.sendPacket(playerIU);
		return true;
	}

	public synchronized TradeItem removeItem(int objectId, int itemId, int count)
	{
		if (isLocked())
		{
			_log.warn(_owner.getName() + ": Attempt to modify locked TradeList!");
			return null;
		}

		for (TradeItem titem : _items)
			if (titem.getObjectId() == objectId || titem.getItem().getItemId() == itemId)
			{
				if (_partner != null)
				{
					TradeList partnerList = _partner.getActiveTradeList();
					if (partnerList == null)
					{
						_log.warn(_partner.getName() + ": Trading partner (" + _partner.getName() + ") is invalid in this trade!");
						return null;
					}
					partnerList.invalidateConfirmation();
				}

				if (count != -1 && titem.getCount() > count)
				{
					titem.setCount(titem.getCount() - count);
				}
				else
				{
					_items.remove(titem);
				}

				return titem;
			}
		return null;
	}

	public void setPackaged(boolean value)
	{
		_packaged = value;
	}

	public void setPartner(L2PcInstance partner)
	{
		_partner = partner;
	}

	public void setTitle(String title)
	{
		_title = title;
	}

	private boolean TransferItems(L2PcInstance partner, InventoryUpdate ownerIU, InventoryUpdate partnerIU)
	{
		for (TradeItem titem : _items)
		{
			L2ItemInstance oldItem = _owner.getInventory().getItemByObjectId(titem.getObjectId());
			if (oldItem == null)
				return false;

			L2ItemInstance newItem = _owner.getInventory().transferItem("Trade", titem.getObjectId(), titem.getCount(), partner.getInventory(), _owner, _partner);
			if (newItem == null)
				return false;

			if (ownerIU != null)
				if (oldItem.getCount() > 0 && oldItem != newItem)
				{
					ownerIU.addModifiedItem(oldItem);
				}
				else
				{
					ownerIU.addRemovedItem(oldItem);
				}

			if (partnerIU != null)
				if (newItem.getCount() > titem.getCount())
				{
					partnerIU.addModifiedItem(newItem);
				}
				else
				{
					partnerIU.addNewItem(newItem);
				}

			oldItem = null;
			newItem = null;
		}
		return true;
	}

	public synchronized void updateItems()
	{
		for (TradeItem titem : _items)
		{
			L2ItemInstance item = _owner.getInventory().getItemByObjectId(titem.getObjectId());
			if (item == null || titem.getCount() < 1)
			{
				removeItem(titem.getObjectId(), -1, -1);
			}
			else if (item.getCount() < titem.getCount())
			{
				titem.setCount(item.getCount());
			}
		}
	}

	private boolean validate()
	{
		if (_owner == null || L2World.getInstance().getPlayer(_owner.getObjectId()) == null)
		{
			_log.warn("Invalid owner of TradeList");
			return false;
		}

		for (TradeItem titem : _items)
		{
			L2ItemInstance item = _owner.checkItemManipulation(titem.getObjectId(), titem.getCount(), "transfer");
			if (item == null || titem.getCount() < 1)
			{
				_log.warn(_owner.getName() + ": Invalid Item in TradeList");
				return false;
			}
		}

		return true;
	}
}