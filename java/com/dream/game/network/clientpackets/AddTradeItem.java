package com.dream.game.network.clientpackets;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.Shutdown;
import com.dream.game.model.TradeList;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.TradeOtherAdd;
import com.dream.game.network.serverpackets.TradeOwnAdd;
import com.dream.game.network.serverpackets.TradeUpdate;

public class AddTradeItem extends L2GameClientPacket
{
	public static Logger _log = Logger.getLogger(AddTradeItem.class.getName());

	private int _tradeId;
	private int _objectId;
	private int _count;

	@Override
	protected void readImpl()
	{
		_tradeId = readD();
		_objectId = readD();
		_count = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		if (Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_TRANSACTION && Shutdown.getCounterInstance() != null && Shutdown.getCounterInstance().getCountdown() <= Config.SAFE_REBOOT_TIME)
		{
			player.sendPacket(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (player.getInventory().getItemByObjectId(_objectId) == null || _count <= 0)
		{
			_log.warn("Character:" + player.getName() + " requested invalid trade object");
			return;
		}

		TradeList trade = player.getActiveTradeList();
		if (trade == null)
		{
			_log.info("Player: " + player.getName() + " requested item:" + _objectId + " add without active tradelist:" + _tradeId);
			return;
		}

		if (trade.getPartner() == null || L2World.getInstance().getPlayer(trade.getPartner().getObjectId()) == null)
		{
			if (trade.getPartner() != null)
			{
				_log.info("Player:" + player.getName() + " requested invalid trade object: " + _objectId);
			}
			player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			player.cancelActiveTrade();
			return;
		}

		TradeList partnerTrade = trade.getPartner().getActiveTradeList();
		if (partnerTrade == null)
			return;

		if (trade.isConfirmed() || partnerTrade.isConfirmed())
		{
			player.sendPacket(SystemMessageId.ONCE_THE_TRADE_IS_CONFIRMED_THE_ITEM_CANNOT_BE_MOVED_AGAIN);
			return;
		}
		if (!player.validateItemManipulation(_objectId, "trade") && !player.isGM())
		{
			player.sendPacket(SystemMessageId.NOTHING_HAPPENED);
			return;
		}

		final TradeList.TradeItem item = trade.addItem(_objectId, _count);
		if (item == null)
			return;

		if (item.isAugmented())
			return;

		player.sendPacket(new TradeOwnAdd(item));
		player.sendPacket(new TradeUpdate(trade, player));
		trade.getPartner().sendPacket(new TradeOtherAdd(item));
	}

}