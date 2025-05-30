package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.manager.CastleManager;
import com.dream.game.manager.CastleManorManager;
import com.dream.game.manager.CastleManorManager.SeedProduction;
import com.dream.game.model.L2Object;
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

public class RequestBuySeed extends L2GameClientPacket
{
	private int _count, _manorId;
	private int[] _items;

	@Override
	protected void readImpl()
	{
		_manorId = readD();
		_count = readD();
		if (_count > 500 || _count * 8 < _buf.remaining() || _count < 1)
		{
			_count = 0;
			return;
		}

		_items = new int[_count * 2];

		for (int i = 0; i < _count; i++)
		{
			int itemId = readD();
			_items[i * 2] = itemId;
			long cnt = readD();
			if (cnt >= Integer.MAX_VALUE || cnt < 1)
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
		long totalPrice = 0;
		int slots = 0;
		int totalWeight = 0;

		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		if (_count < 1)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2Object target = player.getTarget();

		if (!(target instanceof L2ManorManagerInstance))
		{
			target = player.getLastFolkNPC();
		}

		if (!(target instanceof L2ManorManagerInstance))
			return;
		if (player.getInventoryLimit() - player.getInventory().getSize() <= _count)
		{
			player.sendPacket(SystemMessageId.SLOTS_FULL);
			return;
		}

		Castle castle = CastleManager.getInstance().getCastleById(_manorId);

		for (int i = 0; i < _count; i++)
		{
			int seedId = _items[i * 2];
			int count = _items[i * 2 + 1];
			int price = 0;
			int residual = 0;

			SeedProduction seed = castle.getSeed(seedId, CastleManorManager.PERIOD_CURRENT);
			price = seed.getPrice();
			residual = seed.getCanProduce();

			if (price <= 0)
				return;

			if (residual < count)
				return;

			totalPrice += count * price;

			L2Item template = ItemTable.getInstance().getTemplate(seedId);
			totalWeight += count * template.getWeight();
			if (!template.isStackable())
			{
				slots += count;
			}
			else if (player.getInventory().getItemByItemId(seedId) == null)
			{
				slots++;
			}
		}

		if (totalPrice >= Integer.MAX_VALUE)
		{
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " attempts to exceed the limit for Adena buy.", Config.DEFAULT_PUNISH);
			return;
		}
		if (!player.getInventory().validateWeight(totalWeight))
		{
			player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
			return;
		}
		if (!player.getInventory().validateCapacity(slots))
		{
			player.sendPacket(SystemMessageId.SLOTS_FULL);
			return;
		}

		if (totalPrice < 0 || !player.reduceAdena("Buy", (int) totalPrice, target, false))
		{
			player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			return;
		}

		castle.addToTreasuryNoTax((int) totalPrice);

		InventoryUpdate playerIU = new InventoryUpdate();
		for (int i = 0; i < _count; i++)
		{
			int seedId = _items[i * 2];
			int count = _items[i * 2 + 1];
			if (count < 0)
			{
				count = 0;
			}

			SeedProduction seed = castle.getSeed(seedId, CastleManorManager.PERIOD_CURRENT);
			seed.setCanProduce(seed.getCanProduce() - count);
			if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
			{
				CastleManager.getInstance().getCastleById(_manorId).updateSeed(seed.getId(), seed.getCanProduce(), CastleManorManager.PERIOD_CURRENT);
			}

			L2ItemInstance item = player.getInventory().addItem("Buy", seedId, count, player, target);

			if (item.getCount() > count)
			{
				playerIU.addModifiedItem(item);
			}
			else
			{
				playerIU.addNewItem(item);
			}

			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(item).addNumber(count));
		}

		player.sendPacket(playerIU);

		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}

}