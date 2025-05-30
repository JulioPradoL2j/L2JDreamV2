package com.dream.game.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2NpcInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.itemcontainer.ItemContainer;
import com.dream.game.model.itemcontainer.PcFreight;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.templates.item.L2EtcItemType;

public final class RequestPackageSend extends L2GameClientPacket
{
	private class Item
	{
		public int id;
		public int count;

		public Item(int i, int c)
		{
			id = i;
			count = c;
		}
	}

	public static Logger _log = Logger.getLogger(RequestPackageSend.class.getName());
	private final List<Item> _items = new ArrayList<>();
	private int _objectID, _count;

	private InventoryUpdate playerIU;

	@Override
	protected void readImpl()
	{
		_objectID = readD();
		_count = readD();
		if (_count < 0 || _count > 500)
		{
			_count = -1;
			return;
		}
		for (int i = 0; i < _count; i++)
		{
			int id = readD();
			int count = readD();
			_items.add(new Item(id, count));
		}
	}

	@Override
	protected void runImpl()
	{
		if (_count == -1)
			return;
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		L2PcInstance target = L2PcInstance.load(_objectID);

		try
		{
			PcFreight freight = target.getFreight();
			player.setActiveWarehouse(freight);
			ItemContainer warehouse = player.getActiveWarehouse();
			if (warehouse == null)
				return;
			L2NpcInstance manager = player.getLastFolkNPC();
			if ((manager == null || !player.isInsideRadius(manager, L2Npc.INTERACTION_DISTANCE, false, false)) && !player.isGM())
				return;

			if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && player.getKarma() > 0)
				return;

			int fee = _count * Config.ALT_GAME_FREIGHT_PRICE;
			int currentAdena = player.getAdena();
			int slots = 0;

			for (Item i : _items)
			{
				int objectId = i.id;
				int count = i.count;

				L2ItemInstance item = player.checkItemManipulation(objectId, count, "deposit");
				if (item == null)
				{
					_log.info("Error depositing a warehouse object for char " + player.getName() + ".");
					i.id = 0;
					i.count = 0;
					continue;
				}

				if (!item.isTradeable() || item.getItemType() == L2EtcItemType.QUEST)
					return;

				if (item.getItemId() == 57)
				{
					currentAdena -= count;
				}
				if (!item.isStackable())
				{
					slots += count;
				}
				else if (warehouse.getItemByItemId(item.getItemId()) == null)
				{
					slots++;
				}
			}

			if (!warehouse.validateCapacity(slots))
			{
				player.sendPacket(SystemMessageId.WAREHOUSE_FULL);
				return;
			}

			if (currentAdena < fee || !player.reduceAdena("Warehouse", fee, player.getLastFolkNPC(), false))
			{
				player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
				return;
			}

			playerIU = new InventoryUpdate();
			for (Item i : _items)
			{
				int objectId = i.id;
				int count = i.count;

				if (objectId == 0 && count == 0)
				{
					continue;
				}

				L2ItemInstance oldItem = player.getInventory().getItemByObjectId(objectId);
				if (oldItem == null)
				{
					_log.info("Error depositing a warehouse object for char " + player.getName() + ".");
					continue;
				}

				if (oldItem.isHeroItem())
				{
					continue;
				}

				L2ItemInstance newItem = player.getInventory().transferItem("Warehouse", objectId, count, warehouse, player, player.getLastFolkNPC());
				if (newItem == null)
				{
					_log.info("Error depositing a warehouse object for char " + player.getName() + ".");
					continue;
				}

				if (playerIU != null)
					if (oldItem.getCount() > 0 && oldItem != newItem)
					{
						playerIU.addModifiedItem(oldItem);
					}
					else
					{
						playerIU.addRemovedItem(oldItem);
					}
			}

			if (playerIU != null)
			{
				player.sendPacket(playerIU);
			}
			else
			{
				player.sendPacket(new ItemList(player, false));
			}

			StatusUpdate su = new StatusUpdate(player);
			su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
			player.sendPacket(su);
		}
		finally
		{
			target.deleteMe();
		}
	}

}