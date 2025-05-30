package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.Shutdown;
import com.dream.game.model.TradeList;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;

public class TradeDone extends L2GameClientPacket
{
	private int _response;

	@Override
	protected void readImpl()
	{
		_response = readD();
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
			player.cancelActiveTrade();
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		TradeList trade = player.getActiveTradeList();
		if (trade == null)
			return;

		if (trade.isLocked())
			return;

		if (_response == 1)
		{
			if (trade.getPartner() == null || L2World.getInstance().getPlayer(trade.getPartner().getObjectId()) == null)
			{
				player.cancelActiveTrade();
				player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
				return;
			}

			if (trade.getPartner().isOnline() == 0 || trade.getPartner().getClient() == null)
			{
				player.cancelActiveTrade();
				player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
				return;
			}

			if (!trade.getPartner().isInsideRadius(player, 200, false, false))
			{
				player.cancelActiveTrade();
				player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
				return;
			}

			if (trade.getOwner().getActiveEnchantItem() != null || trade.getPartner().getActiveEnchantItem() != null)
				return;

			trade.confirm();
		}
		else
		{
			player.cancelActiveTrade();
		}
		player.setTrading(false);
	}

}