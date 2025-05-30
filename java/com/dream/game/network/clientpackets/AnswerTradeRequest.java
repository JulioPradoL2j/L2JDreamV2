package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.Shutdown;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.SendTradeDone;
import com.dream.game.network.serverpackets.SystemMessage;

public class AnswerTradeRequest extends L2GameClientPacket
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
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2PcInstance partner = player.getActiveRequester();
		if (partner == null || L2World.getInstance().getPlayer(partner.getObjectId()) == null)
		{
			player.sendPacket(new SendTradeDone(0));
			player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			player.setActiveRequester(null);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (!player.isInsideRadius(partner, 200, false, false))
		{
			player.sendPacket(new SendTradeDone(0));
			player.setActiveRequester(null);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (_response == 1 && !partner.isRequestExpired())
		{
			player.startTrade(partner);
		}
		else
		{
			partner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DENIED_TRADE_REQUEST).addString(player.getName()));
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}

		player.setActiveRequester(null);
		partner.onTransactionResponse();
	}

}