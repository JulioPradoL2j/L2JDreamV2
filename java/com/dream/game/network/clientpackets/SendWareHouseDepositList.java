package com.dream.game.network.clientpackets;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.Shutdown;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2NpcInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.itemcontainer.ClanWarehouse;
import com.dream.game.model.itemcontainer.ItemContainer;
import com.dream.game.model.itemcontainer.PcFreight;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.templates.item.L2EtcItemType;
import com.dream.game.util.IllegalPlayerAction;
import com.dream.game.util.Util;

public class SendWareHouseDepositList extends L2GameClientPacket
{
	public static Logger _log = Logger.getLogger(SendWareHouseDepositList.class.getName());
	private int _count;
	private int[] _items;
	private InventoryUpdate playerIU;

	@Override
	protected void readImpl()
	{
		_count = readD();

		if (_count < 0 || _count * 8 > _buf.remaining() || _count > Config.MAX_ITEM_IN_PACKET)
		{
			_count = 0;
		}

		_items = new int[_count * 2];
		for (int i = 0; i < _count; i++)
		{
			int objectId = readD();
			_items[i * 2] = objectId;
			long cnt = readD();
			if (cnt >= Integer.MAX_VALUE || cnt < 0)
			{
				_count = 0;
				_items = null;
				return;
			}
			_items[i * 2 + 1] = (int) cnt;
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		if (player.isProcessingTransaction())
		{
			player.sendPacket(SystemMessageId.ALREADY_TRADING);
			return;
		}

		player._bbsMultisell = 0;
		ItemContainer warehouse = player.getActiveWarehouse();
		if (warehouse == null)
			return;
		L2NpcInstance manager = player.getLastFolkNPC();
		if (player.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_NONE)
		{
			player.sendPacket(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (player.getTarget() != player.getLastFolkNPC())
		{
			player.sendPacket(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_TRANSACTION && Shutdown.getCounterInstance() != null && Shutdown.getCounterInstance().getCountdown() <= Config.SAFE_REBOOT_TIME)
		{
			player.sendPacket(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if ((manager == null || !player.isInsideRadius(manager, L2Npc.INTERACTION_DISTANCE, false, false)) && !player.isGM())
			return;

		if (player.getActiveEnchantItem() != null)
		{
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " attempts to use the bug with point!", IllegalPlayerAction.PUNISH_KICK);
			return;
		}

		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && player.getKarma() > 0)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		int fee = _count * 30;
		int currentAdena = player.getAdena();
		int slots = 0;

		for (int i = 0; i < _count; i++)
		{
			int objectId = _items[i * 2];
			int count = _items[i * 2 + 1];

			L2ItemInstance item = player.checkItemManipulation(objectId, count, "deposit");
			if (item == null)
			{
				_items[i * 2 + 0] = 0;
				_items[i * 2 + 1] = 0;
				continue;
			}

			if (Config.ALT_STRICT_HERO_SYSTEM)
				if (item.isHeroItem())
				{
					continue;
				}

			if ((warehouse instanceof ClanWarehouse || warehouse instanceof PcFreight) && !item.isTradeable() || item.getItemType() == L2EtcItemType.QUEST)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			int id = item.getItemId();
			if (id == 10612 || id == 10280 || id == 10281 || id == 10282 || id == 10283 || id == 10284 || id == 10285 || id == 10286 || id == 10287 || id == 10288 || id == 10289 || id == 10290 || id == 10291 || id == 10292 || id == 10293 || id == 10294 || id == 13002 || id == 13046 || id == 13047 || id == 13042 || id == 13043 || id == 13044)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

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
		for (int i = 0; i < _count; i++)
		{
			int objectId = _items[i * 2];
			int count = _items[i * 2 + 1];

			if (objectId == 0 && count == 0)
			{
				continue;
			}

			L2ItemInstance oldItem = player.getInventory().getItemByObjectId(objectId);

			if (oldItem == null)
			{
				_log.info("Error depositing a warehouse object for char " + player.getName() + " (olditem == null)");
				continue;
			}

			if (Config.ALT_STRICT_HERO_SYSTEM)
				if (oldItem.isHeroItem())
				{
					continue;
				}

			L2ItemInstance newItem = player.getInventory().transferItem(warehouse instanceof ClanWarehouse ? "ClanWarehouse" : "Warehouse", objectId, count, warehouse, player, player.getLastFolkNPC());
			if (newItem == null)
			{
				_log.info("Error depositing a warehouse object for char " + player.getName() + " (newitem == null)");
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
		player.sendPacket(new InventoryUpdate());
		player.broadcastFullInfo();
		warehouse.updateDatabase();
		player.store(true);
	}

}