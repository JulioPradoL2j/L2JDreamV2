package com.dream.game.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import com.dream.Config;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.manager.CastleManorManager;
import com.dream.game.manager.CastleManorManager.CropProcure;
import com.dream.game.model.L2Manor;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2ManorManagerInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.item.L2Item;
import com.dream.game.util.Util;

public class RequestBuyProcure extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _listId, _count;
	private int[] _items;
	@SuppressWarnings("unused")
	private List<CropProcure> _procureList = new ArrayList<CropProcure>();
	private L2ManorManagerInstance manor;

	@Override
	protected void readImpl()
	{
		_listId = readD();
		_count = readD();
		if (_count > 500)
		{
			_count = 0;
			return;
		}

		_items = new int[_count * 2];
		for (int i = 0; i < _count; i++)
		{
			readD();
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
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		player._bbsMultisell = 0;
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0)
			return;

		L2Object target = player.getTarget();

		if (_count < 1)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		int slots = 0;
		int weight = 0;
		manor = target instanceof L2ManorManagerInstance ? (L2ManorManagerInstance) target : null;
		if (player.getInventoryLimit() - player.getInventory().getSize() <= _count)
		{
			player.sendPacket(SystemMessageId.SLOTS_FULL);
			return;
		}

		for (int i = 0; i < _count; i++)
		{
			int itemId = _items[i * 2];
			int count = _items[i * 2 + 1];
			if (count >= Integer.MAX_VALUE)
			{
				Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " wanted to buy more than number item. Max: " + Integer.MAX_VALUE + ".", Config.DEFAULT_PUNISH);
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
				return;
			}

			L2Item template = ItemTable.getInstance().getTemplate(L2Manor.getInstance().getRewardItem(itemId, manor.getCastle().getCrop(itemId, CastleManorManager.PERIOD_CURRENT).getReward()));
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
		_procureList = manor.getCastle().getCropProcure(CastleManorManager.PERIOD_CURRENT);

		for (int i = 0; i < _count; i++)
		{
			int itemId = _items[i * 2];
			int count = _items[i * 2 + 1];
			if (count < 0)
			{
				count = 0;
			}

			int rewardItemId = L2Manor.getInstance().getRewardItem(itemId, manor.getCastle().getCrop(itemId, CastleManorManager.PERIOD_CURRENT).getReward());

			int rewardItemCount = 1;

			rewardItemCount = count / rewardItemCount;

			L2ItemInstance item = player.getInventory().addItem("Manor", rewardItemId, rewardItemCount, player, manor);
			L2ItemInstance iteme = player.getInventory().destroyItemByItemId("Manor", itemId, count, player, manor);

			if (item == null || iteme == null)
			{
				continue;
			}

			playerIU.addRemovedItem(iteme);
			if (item.getCount() > rewardItemCount)
			{
				playerIU.addModifiedItem(item);
			}
			else
			{
				playerIU.addNewItem(item);
			}

			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(item).addNumber(rewardItemCount));
		}

		player.sendPacket(playerIU);

		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}

}