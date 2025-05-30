package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.Shutdown;
import com.dream.game.model.TradeList;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.olympiad.Olympiad;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.PrivateStoreManageListBuy;
import com.dream.game.network.serverpackets.PrivateStoreMsgBuy;

public class SetPrivateStoreListBuy extends L2GameClientPacket
{
	private int _count;
	private int[] _items;

	@Override
	protected void readImpl()
	{
		_count = readD();
		if (_count <= 0 || _count * 12 > _buf.remaining() || _count > Config.MAX_ITEM_IN_PACKET)
		{
			_count = 0;
			_items = null;
			return;
		}
		_items = new int[_count * 3];
		for (int x = 0; x < _count; x++)
		{
			int itemId = readD();
			_items[x * 3] = itemId;
			readH();
			readH();
			long cnt = readD();
			if (cnt >= Integer.MAX_VALUE || cnt < 0)
			{
				_count = 0;
				_items = null;
				return;
			}
			_items[x * 3 + 1] = (int) cnt;
			int price = readD();
			_items[x * 3 + 2] = price;
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		if (player.isOfflineTrade())
			return;
		player.stopMove();
		
		if (player.getObservMode() != 0 || Olympiad.isRegistered(player) || Olympiad.isRegisteredInComp(player) || player.isArenaProtection())
		{
			player.sendPacket(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (player.isCastingNow())
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
		for (L2PcInstance pc : player.getKnownList().getKnownPlayersInRadius(player.getTemplate().getCollisionRadius() * 2))
			if (pc.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_NONE)
			{
				player.sendMessage("You are too close to another dealer");
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;

			}

		TradeList tradeList = player.getBuyList();
		tradeList.clear();

		int cost = 0;
		for (int i = 0; i < _count; i++)
		{
			int itemId = _items[i * 3];
			int count = _items[i * 3 + 1];
			int price = _items[i * 3 + 2];

			tradeList.addItemByItemId(itemId, count, price);
			cost += count * price;
		}

		if (_count <= 0)
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			player.broadcastUserInfo();
			return;
		}

		if (_count > player.getPrivateBuyStoreLimit())
		{
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}

		if (Config.SELL_BY_ITEM)
		{
			if (cost > player.getItemCount(Config.SELL_ITEM, -1) || cost <= 0)
			{
				player.sendPacket(new PrivateStoreManageListBuy(player));
				player.sendPacket(SystemMessageId.THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY);
				return;
			}
		}
		else
		{
		if (cost > player.getAdena() || cost <= 0)
		{
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(SystemMessageId.THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY);
			return;
		}
		}

		if (Config.CHECK_ZONE_ON_PVT && !player.isInsideZone(L2Zone.FLAG_TRADE))
		{
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (player.isInsideZone(L2Zone.FLAG_NOSTORE))
		{
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (player.getParty() != null)
		{
			player.getParty().removePartyMember(player);
		}
		player.sitDown();
		player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_BUY);
		player.broadcastUserInfo();
		player.broadcastPacket(new PrivateStoreMsgBuy(player));
	}

}