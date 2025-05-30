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
package com.dream.game.model.itemcontainer;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.manager.FortSiegeManager;
import com.dream.game.model.L2Object;
import com.dream.game.model.TradeList;
import com.dream.game.model.TradeList.TradeItem;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2ItemInstance.ItemLocation;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.templates.item.L2EtcItemType;
import com.dream.util.LinkedBunch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PcInventory extends Inventory
{
	public static final int ADENA_ID = 57;

	public static final int ANCIENT_ADENA_ID = 5575;

	
	public static int[][] restoreVisibleInventory(int objectId)
	{
		int[][] paperdoll = new int[31][4];
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement2 = con.prepareStatement("SELECT object_id,item_id,loc_data,enchant_level FROM items WHERE owner_id=? AND loc='PAPERDOLL'");
			statement2.setInt(1, objectId);
			ResultSet invdata = statement2.executeQuery();

			int slot, objId, itemId, enchant, displayId;
			while (invdata.next())
			{
				slot = invdata.getInt("loc_data");
				objId = invdata.getInt("object_id");
				itemId = invdata.getInt("item_id");
				enchant = invdata.getInt("enchant_level");
				displayId = ItemTable.getInstance().getTemplate(itemId).getItemDisplayId();

				paperdoll[slot][0] = objId;
				paperdoll[slot][1] = itemId;
				paperdoll[slot][2] = enchant;
				paperdoll[slot][3] = displayId;
				if (slot == Inventory.PAPERDOLL_LRHAND)
				{
					paperdoll[Inventory.PAPERDOLL_RHAND][0] = objId;
					paperdoll[Inventory.PAPERDOLL_RHAND][1] = itemId;
					paperdoll[Inventory.PAPERDOLL_RHAND][2] = enchant;
					paperdoll[Inventory.PAPERDOLL_RHAND][3] = displayId;
				}
			}

			invdata.close();
			statement2.close();
		}
		catch (Exception e)
		{
			_log.warn("could not restore inventory:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		return paperdoll;
	}

	private final L2PcInstance _owner;
	private L2ItemInstance _adena;
	private L2ItemInstance _ancientAdena;

	private L2ItemInstance item;

	public PcInventory(L2PcInstance owner)
	{
		_owner = owner;
	}

	public void addAdena(String process, int count, L2PcInstance actor, L2Object reference)
	{
		if (count > 0)
		{
			addItem(process, ADENA_ID, count, actor, reference);
		}
	}

	public void addAncientAdena(String process, int count, L2PcInstance actor, L2Object reference)
	{
		if (count > 0)
		{
			addItem(process, ANCIENT_ADENA_ID, count, actor, reference);
		}
	}

	@Override
	public L2ItemInstance addItem(String process, int itemId, int count, L2PcInstance actor, L2Object reference)
	{
		item = super.addItem(process, itemId, count, actor, reference);
		if (item == null)
			return null;
		if (item != null && item.getItemId() == ADENA_ID && !item.equals(_adena))
		{
			_adena = item;
		}

		if (item != null && item.getItemId() == ANCIENT_ADENA_ID && !item.equals(_ancientAdena))
		{
			_ancientAdena = item;
		}
		return item;
	}

	@Override
	public L2ItemInstance addItem(String process, L2ItemInstance item, L2PcInstance actor, L2Object reference)
	{
		item = super.addItem(process, item, actor, reference);

		if (item != null && item.getItemId() == ADENA_ID && !item.equals(_adena))
		{
			_adena = item;
		}

		if (item != null && item.getItemId() == ANCIENT_ADENA_ID && !item.equals(_ancientAdena))
		{
			_ancientAdena = item;
		}
		return item;
	}

	public void adjustAvailableItem(TradeItem item)
	{
		boolean notAllEquipped = false;
		for (L2ItemInstance adjItem : getItemsByItemId(item.getItem().getItemId()))
			if (adjItem.isEquipable())
			{
				if (!adjItem.isEquipped())
				{
					notAllEquipped |= true;
				}
			}
			else
			{
				notAllEquipped |= true;
				break;
			}

		if (notAllEquipped)
		{
			L2ItemInstance adjItem = getItemByItemId(item.getItem().getItemId());
			item.setObjectId(adjItem.getObjectId());
			item.setEnchant(adjItem.getEnchantLevel());

			if (adjItem.getCount() < item.getCount())
			{
				item.setCount(adjItem.getCount());
			}

			return;
		}

		item.setCount(0);
	}

	@Override
	public L2ItemInstance destroyItem(String process, int objectId, int count, L2PcInstance actor, L2Object reference)
	{
		L2ItemInstance item = getItemByObjectId(objectId);
		if (item == null)
			return null;
		return this.destroyItem(process, item, count, actor, reference);
	}

	@Override
	public L2ItemInstance destroyItem(String process, L2ItemInstance item, int count, L2PcInstance actor, L2Object reference)
	{
		item = super.destroyItem(process, item, count, actor, reference);

		if (_adena != null && _adena.getCount() <= 0)
		{
			_adena = null;
		}

		if (_ancientAdena != null && _ancientAdena.getCount() <= 0)
		{
			_ancientAdena = null;
		}
		return item;
	}

	@Override
	public L2ItemInstance destroyItem(String process, L2ItemInstance item, L2PcInstance actor, L2Object reference)
	{
		return this.destroyItem(process, item, item.getCount(), actor, reference);
	}

	@Override
	public L2ItemInstance destroyItemByItemId(String process, int itemId, int count, L2PcInstance actor, L2Object reference)
	{
		L2ItemInstance item = getItemByItemId(itemId);
		if (item == null)
			return null;
		return this.destroyItem(process, item, count, actor, reference);
	}

	@Override
	public L2ItemInstance dropItem(String process, int objectId, int count, L2PcInstance actor, L2Object reference)
	{
		L2ItemInstance item = super.dropItem(process, objectId, count, actor, reference);

		if (_adena != null && (_adena.getCount() <= 0 || _adena.getOwnerId() != getOwnerId()))
		{
			_adena = null;
		}

		if (_ancientAdena != null && (_ancientAdena.getCount() <= 0 || _ancientAdena.getOwnerId() != getOwnerId()))
		{
			_ancientAdena = null;
		}

		return item;
	}

	@Override
	public L2ItemInstance dropItem(String process, L2ItemInstance item, L2PcInstance actor, L2Object reference)
	{
		item = super.dropItem(process, item, actor, reference);

		if (_adena != null && (_adena.getCount() <= 0 || _adena.getOwnerId() != getOwnerId()))
		{
			_adena = null;
		}

		if (_ancientAdena != null && (_ancientAdena.getCount() <= 0 || _ancientAdena.getOwnerId() != getOwnerId()))
		{
			_ancientAdena = null;
		}

		return item;
	}

	@Override
	public int getAdena()
	{
		return _adena != null ? _adena.getCount() : 0;
	}

	public L2ItemInstance getAdenaInstance()
	{
		return _adena;
	}

	public L2ItemInstance[] getAllItemsByItemId(int itemId)
	{
		LinkedBunch<L2ItemInstance> list = new LinkedBunch<>();
		for (L2ItemInstance item : _items)
			if (item.getItemId() == itemId)
			{
				list.add(item);
			}

		return list.moveToArray(new L2ItemInstance[list.size()]);
	}

	public L2ItemInstance[] getAllItemsByItemId(int itemId, int enchantment)
	{
		LinkedBunch<L2ItemInstance> list = new LinkedBunch<>();
		for (L2ItemInstance item : _items)
			if (item.getItemId() == itemId && item.getEnchantLevel() == enchantment)
			{
				list.add(item);
			}

		return list.moveToArray(new L2ItemInstance[list.size()]);
	}

	public int getAncientAdena()
	{
		return _ancientAdena != null ? _ancientAdena.getCount() : 0;
	}

	public L2ItemInstance getAncientAdenaInstance()
	{
		return _ancientAdena;
	}

	public L2ItemInstance[] getAugmentedItems()
	{
		LinkedBunch<L2ItemInstance> list = new LinkedBunch<>();
		for (L2ItemInstance item : _items)
			if (item != null && item.isAugmented())
			{
				list.add(item);
			}

		return list.moveToArray(new L2ItemInstance[list.size()]);
	}

	public List<L2ItemInstance> getAvailableItems(boolean allowAdena)
	{
		List<L2ItemInstance> list = new ArrayList<>();
		for (L2ItemInstance item : _items)
			if (item != null && item.isAvailable(getOwner(), allowAdena))
			{
				list.add(item);
			}

		return list;
	}

	public TradeList.TradeItem[] getAvailableItems(TradeList tradeList)
	{
		LinkedBunch<TradeList.TradeItem> list = new LinkedBunch<>();
		for (L2ItemInstance item : _items)
			if (item.isAvailable(getOwner(), false))
			{
				TradeList.TradeItem adjItem = tradeList.adjustAvailableItem(item);
				if (adjItem != null)
				{
					list.add(adjItem);
				}
			}

		return list.moveToArray(new TradeList.TradeItem[list.size()]);
	}

	@Override
	protected ItemLocation getBaseLocation()
	{
		return ItemLocation.INVENTORY;
	}

	@Override
	protected ItemLocation getEquipLocation()
	{
		return ItemLocation.PAPERDOLL;
	}

	@Override
	public L2PcInstance getOwner()
	{
		return _owner;
	}

	public L2ItemInstance[] getUniqueItems(boolean allowAdena, boolean allowAncientAdena)
	{
		return getUniqueItems(allowAdena, allowAncientAdena, true);
	}

	public L2ItemInstance[] getUniqueItems(boolean allowAdena, boolean allowAncientAdena, boolean onlyAvailable)
	{
		List<L2ItemInstance> list = new ArrayList<>();
		for (L2ItemInstance item : _items)
		{
			if (!allowAdena && item.getItemId() == 57)
			{
				continue;
			}
			if (!allowAncientAdena && item.getItemId() == 5575)
			{
				continue;
			}
			boolean isDuplicate = false;
			for (L2ItemInstance litem : list)
				if (litem.getItemId() == item.getItemId())
				{
					isDuplicate = true;
					break;
				}
			if (!isDuplicate && (!onlyAvailable || item.isSellable() && item.isAvailable(getOwner(), false)))
			{
				list.add(item);
			}
		}

		return list.toArray(new L2ItemInstance[list.size()]);
	}

	public L2ItemInstance[] getUniqueItemsByEnchantLevel(boolean allowAdena, boolean allowAncientAdena)
	{
		return getUniqueItemsByEnchantLevel(allowAdena, allowAncientAdena, true);
	}

	public L2ItemInstance[] getUniqueItemsByEnchantLevel(boolean allowAdena, boolean allowAncientAdena, boolean onlyAvailable)
	{
		List<L2ItemInstance> list = new ArrayList<>();
		for (L2ItemInstance item : _items)
		{
			if (!allowAdena && item.getItemId() == 57)
			{
				continue;
			}
			if (!allowAncientAdena && item.getItemId() == 5575)
			{
				continue;
			}

			boolean isDuplicate = false;
			for (L2ItemInstance litem : list)
				if (litem.getItemId() == item.getItemId() && litem.getEnchantLevel() == item.getEnchantLevel())
				{
					isDuplicate = true;
					break;
				}
			if (!isDuplicate && (!onlyAvailable || item.isSellable() && item.isAvailable(getOwner(), false)))
			{
				list.add(item);
			}
		}

		return list.toArray(new L2ItemInstance[list.size()]);
	}

	public void reduceAdena(String process, int count, L2PcInstance actor, L2Object reference)
	{
		if (count > 0)
		{
			destroyItemByItemId(process, ADENA_ID, count, actor, reference);
		}
	}

	public void reduceAncientAdena(String process, int count, L2PcInstance actor, L2Object reference)
	{
		if (count > 0)
		{
			destroyItemByItemId(process, ANCIENT_ADENA_ID, count, actor, reference);
		}
	}

	@Override
	public void refreshWeight()
	{
		super.refreshWeight();
		getOwner().refreshOverloaded();
	}

	@Override
	protected boolean removeItem(L2ItemInstance item)
	{
		getOwner().removeItemFromShortCut(item.getObjectId());

		if (item.equals(getOwner().getActiveEnchantItem()))
		{
			getOwner().setActiveEnchantItem(null);
		}

		if (item.getItemId() == ADENA_ID)
		{
			_adena = null;
		}
		else if (item.getItemId() == ANCIENT_ADENA_ID)
		{
			_ancientAdena = null;
		}

		return super.removeItem(item);
	}

	@Override
	public void restore()
	{
		super.restore();
		_adena = getItemByItemId(ADENA_ID);
		_ancientAdena = getItemByItemId(ANCIENT_ADENA_ID);
	}

	@Override
	public synchronized L2ItemInstance setPaperdollItem(int slot, L2ItemInstance item)
	{
		if (getPaperdollItem(slot) != null && getPaperdollItem(slot).getItemId() == Config.FORTSIEGE_COMBAT_FLAG_ID && FortSiegeManager.getSiege(getOwner()) != null)
			if (getOwner().getClan().getLeaderId() != getOwner().getObjectId())
			{
				getOwner().removeSkill(246, false);
			}
		if (item != null && item.getItemId() == Config.FORTSIEGE_COMBAT_FLAG_ID && FortSiegeManager.getSiege(getOwner()) != null)
			if (getOwner().getClan().getLeaderId() != getOwner().getObjectId())
			{
				getOwner().addSkill(SkillTable.getInstance().getInfo(246, 1), false);
			}
		L2ItemInstance result = super.setPaperdollItem(slot, item);
		return result;
	}

	@Override
	public L2ItemInstance transferItem(String process, int objectId, int count, ItemContainer target, L2PcInstance actor, L2Object reference)
	{
		L2ItemInstance item = super.transferItem(process, objectId, count, target, actor, reference);

		if (_adena != null && (_adena.getCount() <= 0 || _adena.getOwnerId() != getOwnerId()))
		{
			_adena = null;
		}

		if (_ancientAdena != null && (_ancientAdena.getCount() <= 0 || _ancientAdena.getOwnerId() != getOwnerId()))
		{
			_ancientAdena = null;
		}

		return item;
	}

	@Override
	public void updateInventory(L2ItemInstance newItem)
	{
		if (newItem == null)
			return;
		L2PcInstance targetPlayer = getOwner();
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(newItem);
			targetPlayer.sendPacket(playerIU);
			playerIU = null;
		}

		if (newItem.getItem().getWeight() <= 0)
			return;
		StatusUpdate playerSU = new StatusUpdate(targetPlayer);
		playerSU.addAttribute(StatusUpdate.CUR_LOAD, targetPlayer.getCurrentLoad());
		targetPlayer.sendPacket(playerSU);
		playerSU = null;
	}

	@Override
	public boolean validateCapacity(int slots)
	{
		return _items.size() + slots <= _owner.getInventoryLimit();
	}

	public boolean validateCapacity(L2ItemInstance item)
	{
		int slots = 0;

		if (!(item.isStackable() && getItemByItemId(item.getItemId()) != null) && item.getItemType() != L2EtcItemType.HERB)
		{
			slots++;
		}

		return validateCapacity(slots);
	}

	public boolean validateCapacity(List<L2ItemInstance> items)
	{
		int slots = 0;

		for (L2ItemInstance item : items)
			if (!(item.isStackable() && getItemByItemId(item.getItemId()) != null))
			{
				slots++;
			}

		return validateCapacity(slots);
	}

	public boolean validateCapacityByItemId(int ItemId)
	{
		int slots = 0;

		L2ItemInstance invItem = getItemByItemId(ItemId);
		if (!(invItem != null && invItem.isStackable()))
		{
			slots++;
		}

		return validateCapacity(slots);
	}

	@Override
	public boolean validateWeight(int weight)
	{
		return _totalWeight + weight <= _owner.getMaxLoad();
	}
}