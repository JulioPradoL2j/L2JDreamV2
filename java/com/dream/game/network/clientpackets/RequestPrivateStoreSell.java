package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.Shutdown;
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

public class RequestPrivateStoreSell extends L2GameClientPacket
{
	private int _storePlayerId, _count, _price;
	private ItemRequest[] _items;

	@Override
	protected void readImpl()
	{
		_storePlayerId = readD();
		_count = readD();
		if (_count < 0 || _count * 20 > _buf.remaining() || _count > Config.MAX_ITEM_IN_PACKET)
		{
			_count = 0;
		}
		_items = new ItemRequest[_count];

		long priceTotal = 0;
		for (int i = 0; i < _count; i++)
		{
			int objectId = readD();
			int itemId = readD();
			readH();
			readH();
			long count = readD();
			int price = readD();

			if (count >= Integer.MAX_VALUE || count < 0)
			{
				String msgErr = "[RequestPrivateStoreSell] player " + getClient().getActiveChar().getName() + " tried an overflow exploit, ban this player!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				_count = 0;
				_items = null;
				return;
			}
			_items[i] = new ItemRequest(objectId, itemId, (int) count, price);
			priceTotal += price * count;
		}

		if (priceTotal < 0 || priceTotal >= Integer.MAX_VALUE)
		{
			String msgErr = "[RequestPrivateStoreSell] player " + getClient().getActiveChar().getName() + " tried an overflow exploit, ban this player!";
			Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
			_count = 0;
			_items = null;
			return;
		}

		_price = (int) priceTotal;
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

		if (storePlayer.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_BUY)
			return;

		TradeList storeList = storePlayer.getBuyList();
		if (storeList == null)
			return;

		if (Config.SELL_BY_ITEM)
		{
			if (storePlayer.getItemCount(Config.SELL_ITEM, -1) < _price)
			{
				sendPacket(SystemMessage.sendString("You have not enough items to buy, canceling PrivateBuy"));
				sendPacket(ActionFailed.STATIC_PACKET);
				storePlayer.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
				storePlayer.broadcastUserInfo();
				return;
			}

		}
		else
		{
		if (storePlayer.getAdena() < _price)
		{
			player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			storePlayer.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			storePlayer.broadcastUserInfo();
			return;
		}
		}

		if (!storeList.privateStoreSell(player, _items))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
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