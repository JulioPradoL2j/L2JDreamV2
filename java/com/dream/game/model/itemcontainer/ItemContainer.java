package com.dream.game.model.itemcontainer;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.game.GameTimeController;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2ItemInstance.ItemLocation;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.templates.item.L2Item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

public abstract class ItemContainer
{
	protected static final Logger _log = Logger.getLogger(ItemContainer.class.getName());

	protected final List<L2ItemInstance> _items;

	protected ItemContainer()
	{
		_items = new CopyOnWriteArrayList<>();
	}

	protected void addItem(L2ItemInstance item)
	{
		synchronized (_items)
		{
			_items.add(item);
		}
	}

	public L2ItemInstance addItem(String process, int itemId, int count, L2PcInstance actor, L2Object reference)
	{
		L2ItemInstance item = getItemByItemId(itemId);

		if (item != null && item.isStackable())
		{
			item.changeCount(process, count, actor, reference);
			item.setLastChange(L2ItemInstance.MODIFIED);
			if (itemId == 57 && count < 10000 * Config.RATE_DROP_ADENA)
			{
				if (GameTimeController.getGameTicks() % 5 == 0)
				{
					item.updateDatabase();
				}
			}
			else
			{
				item.updateDatabase();
			}
		}
		else
		{
			for (int i = 0; i < count; i++)
			{
				L2Item template = ItemTable.getInstance().getTemplate(itemId);
				if (template == null)
				{
					_log.info((actor != null ? "[" + actor.getName() + "] " : "") + "Invalid ItemId requested: " + itemId);
					return null;
				}

				item = ItemTable.createItem(process, itemId, template.isStackable() ? count : 1, actor, reference);
				item.setOwnerId(process, getOwnerId(), actor, reference);
				item.setLocation(getBaseLocation());
				item.setLastChange(L2ItemInstance.ADDED);

				addItem(item);
				item.updateDatabase();

				if (template.isStackable())
				{
					break;
				}
			}
		}

		refreshWeight();
		return item;
	}

	public L2ItemInstance addItem(String process, L2ItemInstance item, L2PcInstance actor, L2Object reference)
	{
		L2ItemInstance olditem = getItemByItemId(item.getItemId());

		if (olditem != null && olditem.isStackable())
		{
			int count = item.getCount();
			olditem.changeCount(process, count, actor, reference);
			olditem.setLastChange(L2ItemInstance.MODIFIED);

			ItemTable.destroyItem(process, item, actor, reference);
			item.updateDatabase();
			item = olditem;

			if (item.getItemId() == 57 && count < 10000 * Config.RATE_DROP_ADENA)
			{
				if (GameTimeController.getGameTicks() % 5 == 0)
				{
					item.updateDatabase();
				}
			}
			else
			{
				item.updateDatabase();
			}
		}
		else
		{
			item.setOwnerId(process, getOwnerId(), actor, reference);
			item.setLocation(getBaseLocation());
			item.setLastChange(L2ItemInstance.ADDED);

			addItem(item);

			item.updateDatabase();
		}

		refreshWeight();
		return item;
	}

	public L2ItemInstance addWearItem(String process, int itemId, L2PcInstance actor, L2Object reference)
	{
		L2ItemInstance item = getItemByItemId(itemId);

		if (item != null)
			return item;

		item = ItemTable.createItem(process, itemId, 1, actor, reference);
		item.setWear(true);
		item.setOwnerId(process, getOwnerId(), actor, reference);
		item.setLocation(getBaseLocation());
		item.setLastChange(L2ItemInstance.ADDED);
		addItem(item);
		refreshWeight();

		return item;
	}

	public void deleteMe()
	{
		try
		{
			updateDatabase();
		}
		catch (Exception e)
		{
			_log.fatal("Unable to delete item");
			e.printStackTrace();
		}

		synchronized (_items)
		{
			List<L2Object> items = new ArrayList<>(_items);
			_items.clear();
			L2World.getInstance().removeObjects(items);
		}
	}

	public synchronized void destroyAllItems(String process, L2PcInstance actor, L2Object reference)
	{
		synchronized (_items)
		{
			for (L2ItemInstance item : _items)
			{
				destroyItem(process, item, actor, reference);
			}
		}
	}

	public L2ItemInstance destroyItem(String process, int objectId, int count, L2PcInstance actor, L2Object reference)
	{
		L2ItemInstance item = getItemByObjectId(objectId);
		if (item == null)
			return null;

		return destroyItem(process, item, count, actor, reference);
	}

	public L2ItemInstance destroyItem(String process, L2ItemInstance item, int count, L2PcInstance actor, L2Object reference)
	{
		synchronized (item)
		{
			if (item.getCount() > count)
			{

				item.changeCount(process, -count, actor, reference);
				item.setLastChange(L2ItemInstance.MODIFIED);

				if (process != null || GameTimeController.getGameTicks() % 10 == 0)
				{
					item.updateDatabase();
				}

				refreshWeight();

				return item;
			}

			if (item.getCount() < count)
				return null;

			boolean removed = removeItem(item);
			if (!removed)
				return null;

			ItemTable.destroyItem(process, item, actor, reference);
			item.updateDatabase();
			refreshWeight();
		}

		return item;
	}

	public L2ItemInstance destroyItem(String process, L2ItemInstance item, L2PcInstance actor, L2Object reference)
	{
		return destroyItem(process, item, item.getCount(), actor, reference);
	}

	public L2ItemInstance destroyItemByItemId(String process, int itemId, int count, L2PcInstance actor, L2Object reference)
	{
		L2ItemInstance item = getItemByItemId(itemId);
		if (item == null)
			return null;

		return destroyItem(process, item, count, actor, reference);
	}

	public int getAdena()
	{
		int count = 0;
		for (L2ItemInstance item : _items)
		{
			if (item != null && item.getItemId() == 57)
			{
				count = item.getCount();
				return count;
			}
		}
		return count;
	}

	protected abstract ItemLocation getBaseLocation();

	public int getInventoryItemCount(int itemId, int enchantLevel)
	{
		int count = 0;
		synchronized (_items)
		{
			for (L2ItemInstance item : _items)
				if (item.getItemId() == itemId && (item.getEnchantLevel() == enchantLevel || enchantLevel < 0))
					if (item.isStackable())
					{
						count = item.getCount();
					}
					else
					{
						count++;
					}
		}
		return count;
	}

	public L2ItemInstance getItemByItemId(int itemId)
	{
		synchronized (_items)
		{
			for (L2ItemInstance item : _items)
				if (item != null && item.getItemId() == itemId)
					return item;
		}
		return null;
	}

	public L2ItemInstance getItemByItemId(int itemId, L2ItemInstance itemToIgnore)
	{
		synchronized (_items)
		{
			for (L2ItemInstance item : _items)
				if (item != null && item.getItemId() == itemId && !item.equals(itemToIgnore))
					return item;
		}
		return null;
	}

	public L2ItemInstance getItemByObjectId(int objectId)
	{
		synchronized (_items)
		{
			for (L2ItemInstance item : _items)
				if (item != null && item.getObjectId() == objectId)
					return item;
		}
		return null;
	}

	public L2ItemInstance[] getItems()
	{
		synchronized (_items)
		{
			return _items.toArray(new L2ItemInstance[_items.size()]);
		}
	}

	public List<L2ItemInstance> getItemsByItemId(int itemId)
	{
		List<L2ItemInstance> returnList = new ArrayList<>();
		synchronized (_items)
		{
			for (L2ItemInstance item : _items)
				if (item != null && item.getItemId() == itemId)
				{
					returnList.add(item);
				}
		}
		return returnList;
	}

	protected abstract L2Character getOwner();

	public int getOwnerId()
	{
		return getOwner() == null ? 0 : getOwner().getObjectId();
	}

	public int getSize()
	{
		return _items.size();
	}

	public int getUnequippedSize()
	{
		int count = 0;
		synchronized (_items)
		{
			for (L2ItemInstance temp : _items)
				if (!temp.isEquipped())
				{
					count++;
				}
		}

		return count;
	}

	protected void refreshWeight()
	{

	}

	protected boolean removeItem(L2ItemInstance item)
	{
		synchronized (_items)
		{
			return _items.remove(item);
		}
	}

	
	public void restore()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT * FROM items WHERE owner_id=? AND (loc=?)");
			statement.setInt(1, getOwnerId());
			statement.setString(2, getBaseLocation().name());
			ResultSet inv = statement.executeQuery();

			L2ItemInstance item;
			while (inv.next())
			{
				item = L2ItemInstance.restoreFromDb(getOwnerId(), inv);
				if (item == null)
				{
					continue;
				}

				L2World.getInstance().storeObject(item);

				if (item.isStackable() && getItemByItemId(item.getItemId()) != null)
				{
					addItem("Restore", item, null, getOwner());
				}
				else
				{
					addItem(item);
				}
			}
			inv.close();
			statement.close();
			refreshWeight();
		}
		catch (Exception e)
		{
			_log.warn("could not restore container:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public L2ItemInstance transferItem(String process, int objectId, int count, ItemContainer target, L2PcInstance actor, L2Object reference)
	{
		if (target == null)
			return null;

		L2ItemInstance existsItem = target.getItemByObjectId(objectId);
		if (existsItem != null)
			return existsItem;

		L2ItemInstance sourceitem = getItemByObjectId(objectId);
		if (sourceitem == null)
			return null;
		L2ItemInstance targetitem = sourceitem.isStackable() ? target.getItemByItemId(sourceitem.getItemId()) : null;

		synchronized (sourceitem)
		{
			if (getItemByObjectId(objectId) != sourceitem)
				return null;

			if (count > sourceitem.getCount())
			{
				count = sourceitem.getCount();
			}

			if (sourceitem.getCount() == count && targetitem == null)
			{
				removeItem(sourceitem);
				target.addItem(process, sourceitem, actor, reference);
				targetitem = sourceitem;
			}
			else
			{
				if (sourceitem.getCount() > count)
				{
					sourceitem.changeCount(process, -count, actor, reference);
					sourceitem.setLastChange(L2ItemInstance.MODIFIED);
				}
				else
				{
					removeItem(sourceitem);
					sourceitem.setLastChange(L2ItemInstance.REMOVED);
					ItemTable.destroyItem(process, sourceitem, actor, reference);
				}

				if (targetitem != null)
				{
					targetitem.changeCount(process, count, actor, reference);
					targetitem.setLastChange(L2ItemInstance.MODIFIED);
				}
				else
				{
					targetitem = target.addItem(process, sourceitem.getItemId(), count, actor, reference);
					targetitem.setLastChange(L2ItemInstance.ADDED);
				}
			}
			sourceitem.updateDatabase(true);
			if (targetitem != sourceitem)
				if (target instanceof ClanWarehouse)
				{
					targetitem.updateDatabase(true);
				}
				else
				{
					targetitem.updateDatabase();
				}

			if (sourceitem.isAugmented())
			{
				sourceitem.getAugmentation().removeBonus(actor);
			}
			refreshWeight();
			actor.getInventory().refreshWeight();
			if (reference instanceof L2PcInstance)
			{
				((L2PcInstance) reference).getInventory().refreshWeight();
			}
		}
		return targetitem;
	}

	public void updateDatabase()
	{
		if (getOwner() != null)
		{
			synchronized (_items)
			{
				for (L2ItemInstance item : _items)
					if (item != null)
					{
						item.updateDatabase(true);
					}
			}
		}
	}

	public boolean validateCapacity(int slots)
	{
		return true;
	}

	public boolean validateWeight(int weight)
	{
		return true;
	}
}