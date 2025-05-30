package com.dream.game.handler.item;

import com.dream.game.handler.IItemHandler;
import com.dream.game.manager.CastleManager;
import com.dream.game.manager.MercTicketManager;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.sevensigns.SevenSigns;
import com.dream.game.model.entity.siege.Castle;
import com.dream.game.network.SystemMessageId;

public class MercTicket implements IItemHandler
{
	// left in here for backward compatibility
	@Override
	public int[] getItemIds()
	{
		return MercTicketManager.getInstance().getItemIds();
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		int itemId = item.getItemId();
		L2PcInstance activeChar = (L2PcInstance) playable;
		Castle castle = CastleManager.getInstance().getCastle(activeChar);
		int castleId = -1;
		if (castle != null)
		{
			castleId = castle.getCastleId();
		}

		// add check that certain tickets can only be placed in certain castles
		if (MercTicketManager.getTicketCastleId(itemId) != castleId)
		{
			activeChar.sendPacket(SystemMessageId.MERCENARIES_CANNOT_BE_POSITIONED_HERE);
			return;
		}

		if (!activeChar.isCastleLord(castleId))
		{
			activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_AUTHORITY_TO_POSITION_MERCENARIES);
			return;
		}

		if (castle != null && castle.getSiege().getIsInProgress())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_POSITION_MERCS_DURING_SIEGE);
			return;
		}

		// Checking Seven Signs Quest Period
		if (SevenSigns.getInstance().getCurrentPeriod() != SevenSigns.PERIOD_SEAL_VALIDATION)
		{
			activeChar.sendPacket(SystemMessageId.MERC_CAN_BE_ASSIGNED);
			return;
		}
		// Checking the Seal of Strife status
		switch (SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE))
		{
			case SevenSigns.CABAL_NULL:
				if (SevenSigns.getInstance().checkIsDawnPostingTicket(itemId))
				{
					activeChar.sendPacket(SystemMessageId.MERC_CAN_BE_ASSIGNED);
					return;
				}
				break;
			case SevenSigns.CABAL_DUSK:
				if (!SevenSigns.getInstance().checkIsRookiePostingTicket(itemId))
				{
					activeChar.sendPacket(SystemMessageId.MERC_CAN_BE_ASSIGNED);
					return;
				}
				break;
			case SevenSigns.CABAL_DAWN:
				break;
		}

		if (MercTicketManager.getInstance().isAtCasleLimit(item.getItemId()))
		{
			activeChar.sendPacket(SystemMessageId.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE);
			return;
		}
		if (MercTicketManager.getInstance().isAtTypeLimit(item.getItemId()))
		{
			activeChar.sendPacket(SystemMessageId.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE);
			return;
		}
		if (MercTicketManager.getInstance().isTooCloseToAnotherTicket(activeChar.getX(), activeChar.getY(), activeChar.getZ()))
		{
			activeChar.sendPacket(SystemMessageId.POSITIONING_CANNOT_BE_DONE_BECAUSE_DISTANCE_BETWEEN_MERCENARIES_TOO_SHORT);
			return;
		}

		MercTicketManager.getInstance().addTicket(item.getItemId(), activeChar);
		activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean par)
	{
	}
}