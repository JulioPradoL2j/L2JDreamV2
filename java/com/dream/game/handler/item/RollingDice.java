package com.dream.game.handler.item;

import com.dream.game.handler.IItemHandler;
import com.dream.game.manager.TownManager;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.Dice;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.util.Broadcast;
import com.dream.game.util.FloodProtector;
import com.dream.game.util.FloodProtector.Protected;
import com.dream.tools.random.Rnd;

public class RollingDice implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		4625,
		4626,
		4627,
		4628
	};

	private static int rollDice(L2PcInstance player)
	{
		if (!FloodProtector.tryPerformAction(player, Protected.ROLLDICE))
			return 0;

		return Rnd.get(1, 6);
	}

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		L2PcInstance activeChar = (L2PcInstance) playable;
		int itemId = item.getItemId();

		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return;
		}

		if (itemId == 4625 || itemId == 4626 || itemId == 4627 || itemId == 4628)
		{
			int number = rollDice(activeChar);
			if (number == 0)
			{
				activeChar.sendPacket(SystemMessageId.YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIME_TRY_AGAIN_LATER);
				return;
			}
			Broadcast.toSelfAndKnownPlayers(activeChar, new Dice(activeChar.getObjectId(), item.getItemId(), number, activeChar.getX() - 30, activeChar.getY() - 30, activeChar.getZ()));
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ROLLED_S2).addString(activeChar.getName()).addNumber(number));
			if (!TownManager.getInstance().checkIfInZone(activeChar))
			{
				Broadcast.toKnownPlayers(activeChar, SystemMessage.getSystemMessage(SystemMessageId.S1_ROLLED_S2).addString(activeChar.getName()).addNumber(number));
			}
			else if (activeChar.isInParty())
			{
				activeChar.getParty().broadcastToPartyMembers(activeChar, SystemMessage.getSystemMessage(SystemMessageId.S1_ROLLED_S2).addString(activeChar.getName()).addNumber(number));
			}
		}
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean par)
	{
	}
}