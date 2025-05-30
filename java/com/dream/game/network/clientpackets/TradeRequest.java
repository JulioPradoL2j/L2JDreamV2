package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.Shutdown;
import com.dream.game.model.BlockList;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.SendTradeRequest;
import com.dream.game.network.serverpackets.SystemMessage;

public class TradeRequest extends L2GameClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		player._bbsMultisell = 0;
		if (Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_TRANSACTION && Shutdown.getCounterInstance() != null && Shutdown.getCounterInstance().getCountdown() <= Config.SAFE_REBOOT_TIME)
		{
			player.sendPacket(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2Object obj = null;

		if (player.getTargetId() == _objectId)
		{
			obj = player.getTarget();
		}

		if (obj == null)
		{
			obj = L2World.getInstance().getPlayer(_objectId);
		}

		if (!(obj instanceof L2PcInstance) || obj.getObjectId() == player.getObjectId())
		{
			player.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}

		L2PcInstance partner = (L2PcInstance) obj;

		if (partner.isInOlympiadMode() || player.isInOlympiadMode())
			return;

		if (BlockList.isBlocked(partner, player))
		{
			player.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}

		if (player.getDistanceSq(partner) > 22500)
		{
			player.sendPacket(SystemMessageId.TARGET_TOO_FAR);
			return;
		}

		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && (player.getKarma() > 0 || partner.getKarma() > 0))
		{
			player.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}

		if (player.getPrivateStoreType() != 0 || partner.getPrivateStoreType() != 0)
		{
			player.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return;
		}

		if (player.isProcessingTransaction())
		{
			player.sendPacket(SystemMessageId.ALREADY_TRADING);
			return;
		}

		if (partner.isProcessingRequest() || partner.isProcessingTransaction() || partner.getTradeRefusal() || partner.isInCombat() || partner.isCastingNow() || partner.isTeleporting())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(partner.getName()));
			return;
		}

		player.onTransactionRequest(partner);
		partner.sendPacket(new SendTradeRequest(player.getObjectId()));
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REQUEST_S1_FOR_TRADE).addString(partner.getName()));
	}

}