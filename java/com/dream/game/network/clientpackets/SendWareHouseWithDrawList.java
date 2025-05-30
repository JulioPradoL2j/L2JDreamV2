package com.dream.game.network.clientpackets;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.Shutdown;
import com.dream.game.model.L2Clan;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2NpcInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.itemcontainer.ClanWarehouse;
import com.dream.game.model.itemcontainer.ItemContainer;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.StatusUpdate;

public class SendWareHouseWithDrawList extends L2GameClientPacket
{
	public static Logger _log = Logger.getLogger(SendWareHouseWithDrawList.class.getName());
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
			_items = null;
			return;
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
		if (Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_TRANSACTION && Shutdown.getCounterInstance() != null && Shutdown.getCounterInstance().getCountdown() <= Config.SAFE_REBOOT_TIME)
		{
			player.sendPacket(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (player.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_NONE)
		{
			player.sendPacket(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		ItemContainer warehouse = player.getActiveWarehouse();
		if (warehouse == null)
			return;

		if (warehouse instanceof ClanWarehouse && player.getClanId() != warehouse.getOwnerId())
			return;

		L2NpcInstance manager = player.getLastFolkNPC();
		if (manager != player.getTarget() || (manager == null || !player.isInsideRadius(manager, L2Npc.INTERACTION_DISTANCE, false, false)) && !player.isGM())
			return;

		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && player.getKarma() > 0)
			return;

		if (Config.ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH)
		{
			if (warehouse instanceof ClanWarehouse && !((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) == L2Clan.CP_CL_VIEW_WAREHOUSE))
				return;
		}
		else if (warehouse instanceof ClanWarehouse && !player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CAN_RETRIEVE_ITEMS_FROM_CLAN_WAREHOUSE);
			return;
		}

		int weight = 0;
		int slots = 0;

		for (int i = 0; i < _count; i++)
		{
			int objectId = _items[i * 2];
			int count = _items[i * 2 + 1];

			L2ItemInstance item = warehouse.getItemByObjectId(objectId);
			if (item == null)
			{
				continue;
			}
			weight += count * item.getItem().getWeight();
			if (!item.isStackable())
			{
				slots += count;
			}
			else if (player.getInventory().getItemByItemId(item.getItemId()) == null)
			{
				slots++;
			}
		}

		if (!player.getInventory().validateCapacity(slots))
		{
			player.sendPacket(SystemMessageId.SLOTS_FULL);
			return;
		}

		if (!player.getInventory().validateWeight(weight))
		{
			player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
			return;
		}

		playerIU = new InventoryUpdate();
		for (int i = 0; i < _count; i++)
		{
			int objectId = _items[i * 2];
			int count = _items[i * 2 + 1];

			L2ItemInstance oldItem = warehouse.getItemByObjectId(objectId);
			if (oldItem == null || oldItem.getCount() < count)
			{
				continue;
			}
			L2ItemInstance newItem = warehouse.transferItem(warehouse instanceof ClanWarehouse ? "ClanWarehouse" : "Warehouse", objectId, count, player.getInventory(), player, player.getLastFolkNPC());
			if (newItem == null)
			{
				_log.info("Error withdrawing a warehouse object for char " + player.getName());
				continue;
			}

			if (playerIU != null)
				if (newItem.getCount() > count)
				{
					playerIU.addModifiedItem(newItem);
				}
				else
				{
					playerIU.addNewItem(newItem);
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
	}

}