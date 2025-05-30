package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.manager.CastleManager;
import com.dream.game.manager.CastleManorManager;
import com.dream.game.manager.CastleManorManager.CropProcure;
import com.dream.game.model.L2Manor;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2ManorManagerInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.siege.Castle;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.item.L2Item;
import com.dream.game.util.Util;

public class RequestProcureCropList extends L2GameClientPacket
{
	private int _size;
	private int[] _items;

	@Override
	protected void readImpl()
	{
		_size = readD();
		if (_size * 16 > _buf.remaining() || _size > 500 || _size < 1)
		{
			_size = 0;
			return;
		}

		_items = new int[_size * 4];
		for (int i = 0; i < _size; i++)
		{
			int objId = readD();
			_items[i * 4] = objId;
			int itemId = readD();
			_items[i * 4 + 1] = itemId;
			int manorId = readD();
			_items[i * 4 + 2] = manorId;
			long count = readD();
			if (count > Integer.MAX_VALUE)
			{
				count = Integer.MAX_VALUE;
			}
			_items[i * 4 + 3] = (int) count;
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		L2Object target = player.getTarget();

		if (!(target instanceof L2ManorManagerInstance))
		{
			target = player.getLastFolkNPC();
		}

		if (!player.isGM() && (target == null || !(target instanceof L2ManorManagerInstance) || !player.isInsideRadius(target, L2Npc.INTERACTION_DISTANCE, false, false)))
			return;

		if (_size < 1)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		L2ManorManagerInstance manorManager = (L2ManorManagerInstance) target;

		int currentManorId = manorManager.getCastle().getCastleId();

		int slots = 0;
		int weight = 0;

		for (int i = 0; i < _size; i++)
		{
			int itemId = _items[i * 4 + 1];
			int manorId = _items[i * 4 + 2];
			int count = _items[i * 4 + 3];

			if (itemId == 0 || manorId == 0 || count == 0)
			{
				continue;
			}
			if (count < 1)
			{
				continue;
			}

			if (count > Integer.MAX_VALUE)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " items at the same time.", Config.DEFAULT_PUNISH);
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
				return;
			}

			Castle castle = CastleManager.getInstance().getCastleById(manorId);
			if (castle == null)
			{
				continue;
			}

			CropProcure crop = castle.getCrop(itemId, CastleManorManager.PERIOD_CURRENT);

			if (crop == null)
			{
				continue;
			}

			int rewardItemId = L2Manor.getInstance().getRewardItem(itemId, crop.getReward());

			L2Item template = ItemTable.getInstance().getTemplate(rewardItemId);

			if (template == null)
			{
				continue;
			}

			weight += count * template.getWeight();

			if (!template.isStackable())
			{
				slots += count;
			}
			else if (player.getInventory().getItemByItemId(itemId) == null)
			{
				slots++;
			}
		}

		if (!player.getInventory().validateWeight(weight))
		{
			player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
			return;
		}

		if (!player.getInventory().validateCapacity(slots))
		{
			player.sendPacket(SystemMessageId.SLOTS_FULL);
			return;
		}

		InventoryUpdate playerIU = new InventoryUpdate();

		for (int i = 0; i < _size; i++)
		{
			int objId = _items[i * 4];
			int cropId = _items[i * 4 + 1];
			int manorId = _items[i * 4 + 2];
			int count = _items[i * 4 + 3];

			if (objId == 0 || cropId == 0 || manorId == 0 || count == 0)
			{
				continue;
			}

			if (count < 1)
			{
				continue;
			}

			CropProcure crop = null;

			Castle castle = CastleManager.getInstance().getCastleById(manorId);
			if (castle == null)
			{
				continue;
			}

			crop = castle.getCrop(cropId, CastleManorManager.PERIOD_CURRENT);

			if (crop == null || crop.getId() == 0 || crop.getPrice() == 0)
			{
				continue;
			}

			int fee = 0;

			int rewardItem = L2Manor.getInstance().getRewardItem(cropId, crop.getReward());

			if (count > crop.getAmount())
			{
				continue;
			}

			int sellPrice = count * crop.getPrice();
			int rewardPrice = ItemTable.getInstance().getTemplate(rewardItem).getReferencePrice();

			if (rewardPrice == 0)
			{
				continue;
			}

			int rewardItemCount = sellPrice / rewardPrice;
			if (rewardItemCount < 1)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_IN_TRADING_S2_OF_CROP_S1).addItemName(cropId).addNumber(count));
				continue;
			}

			if (manorId != currentManorId)
			{
				fee = sellPrice * 5 / 100;
			}

			if (player.getInventory().getAdena() < fee)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_IN_TRADING_S2_OF_CROP_S1).addItemName(cropId).addNumber(count));
				player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
				continue;
			}

			L2ItemInstance itemDel = null;
			L2ItemInstance itemAdd = null;
			if (player.getInventory().getItemByObjectId(objId) != null)
			{
				L2ItemInstance item = player.getInventory().getItemByObjectId(objId);
				if (item.getCount() < count)
				{
					continue;
				}

				itemDel = player.getInventory().destroyItem("Manor", objId, count, player, manorManager);
				if (itemDel == null)
				{
					continue;
				}

				if (fee > 0)
				{
					player.getInventory().reduceAdena("Manor", fee, player, manorManager);
				}

				crop.setAmount(crop.getAmount() - count);
				if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
				{
					CastleManager.getInstance().getCastleById(manorId).updateCrop(crop.getId(), crop.getAmount(), CastleManorManager.PERIOD_CURRENT);
				}

				itemAdd = player.getInventory().addItem("Manor", rewardItem, rewardItemCount, player, manorManager);
			}
			else
			{
				continue;
			}

			if (itemAdd == null)
			{
				continue;
			}

			playerIU.addRemovedItem(itemDel);
			if (itemAdd.getCount() > rewardItemCount)
			{
				playerIU.addModifiedItem(itemAdd);
			}
			else
			{
				playerIU.addNewItem(itemAdd);
			}

			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TRADED_S2_OF_CROP_S1).addItemName(cropId).addNumber(count));

			if (fee > 0)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ADENA_HAS_BEEN_WITHDRAWN_TO_PAY_FOR_PURCHASING_FEES).addNumber(fee));
			}

			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(cropId).addNumber(count));

			if (fee > 0)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ADENA_DISAPPEARED).addNumber(fee));
			}

			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(rewardItem).addNumber(rewardItemCount));
		}

		player.sendPacket(playerIU);

		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}

}