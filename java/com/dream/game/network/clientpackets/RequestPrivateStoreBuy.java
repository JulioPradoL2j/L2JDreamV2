package com.dream.game.network.clientpackets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dream.Config;
import com.dream.game.Shutdown;
import com.dream.game.manager.BuffShopManager;
import com.dream.game.manager.OfflineManager;
import com.dream.game.model.ItemRequest;
import com.dream.game.model.L2Object;
import com.dream.game.model.TradeList;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.util.Util;

public class RequestPrivateStoreBuy extends L2GameClientPacket
{
	private int _storePlayerId;
	private int _count;
	private ItemRequest[] _items;
	private List<int[]> _buffs = null;

	@Override
	protected void readImpl()
	{
		_storePlayerId = readD();
		_count = readD();
		if (_count < 0 || _count * 12 > _buf.remaining() || _count > Config.MAX_ITEM_IN_PACKET)
		{
			_count = 0;
		}
		_items = new ItemRequest[_count];
		_buffs = new ArrayList<>();

		for (int i = 0; i < _count; i++)
		{
			int objectId = readD();
			long count = readD();
			if (count > Integer.MAX_VALUE)
			{
				count = Integer.MAX_VALUE;
			}
			int price = readD();

			_items[i] = new ItemRequest(objectId, (int) count, price);
			_buffs.add(new int[]
			{
				objectId,
				price
			});
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null || player.isCursedWeaponEquipped())
			return;

		if (Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_TRANSACTION && Shutdown.getCounterInstance() != null && Shutdown.getCounterInstance().getCountdown() <= Config.SAFE_REBOOT_TIME)
		{
			player.sendPacket(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2Object object = null;

		if (player.getTargetId() == _storePlayerId)
		{
			object = player.getTarget();
		}

		if (object == null)
		{
			object = L2World.getInstance().getPlayer(_storePlayerId);
		}

		if (!(object instanceof L2PcInstance))
			return;

		L2PcInstance storePlayer = (L2PcInstance) object;

		if (!(storePlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_SELL || storePlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_PACKAGE_SELL))
			return;

		if (storePlayer.isBizy())
		{
			BuffShopManager.getInstance().broadcastBizy(storePlayer, player);
			return;
		}

		TradeList storeList = storePlayer.getSellList();
		if (storeList == null)
			return;

		Map<Integer, int[]> sellList = storePlayer.getBuffShopSellList();
		if ((storePlayer.isBuffShop()) && (sellList == null))
		{
			return;
		}

		if (player.getInventoryLimit() - player.getInventory().getSize() <= _items.length)
		{
			player.sendPacket(SystemMessageId.SLOTS_FULL);
			return;
		}

		long priceTotal = 0;
		for (ItemRequest ir : _items)
		{
			if ((ir.getCount() > Integer.MAX_VALUE) || (ir.getCount() < 0))
			{
				String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried an overflow exploit, ban this player!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				return;
			}
			priceTotal += ir.getPrice() * ir.getCount();
		}

		if (priceTotal < 0 || priceTotal >= Integer.MAX_VALUE)
		{
			String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried an overflow exploit, ban this player!";
			Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
			return;
		}

		if (Config.SELL_BY_ITEM)
		{
			if (player.getItemCount(Config.SELL_ITEM, -1) < priceTotal)
			{
				sendPacket(SystemMessage.sendString("You do not have needed items to buy"));
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

		}
		else
		{
		if (player.getAdena() < priceTotal)
		{
			player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		}

		if (player.isInOlympiadMode())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (player.inObserverMode())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (storePlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_PACKAGE_SELL)
			if (storeList.getItemCount() > _count)
			{
				String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried to buy less items then sold by package-sell, ban this player for bot-usage!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				return;
			}

		if (!storeList.privateStoreBuy(player, _items, (int) priceTotal))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (storePlayer.isBuffShop())
		{
			BuffShopManager.getInstance().processBuffs(storePlayer, player, _buffs);
			return;
		}

		if (storeList.getItemCount() == 0)
		{
			storePlayer.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			storePlayer.broadcastUserInfo();

			if (storePlayer.isOfflineTrade())
			{
				OfflineManager.getInstance().removeTrader(storePlayer);
			}
		}
	}

}