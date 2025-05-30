package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.datatables.GmListTable;
import com.dream.game.manager.PetitionManager;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.network.serverpackets.SystemMessage;

public class RequestPetitionCancel extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (PetitionManager.getInstance().isPlayerInConsultation(activeChar))
		{
			if (activeChar.isGM())
			{
				PetitionManager.getInstance().endActivePetition(activeChar);
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.PETITION_UNDER_PROCESS);
			}
		}
		else if (PetitionManager.getInstance().isPlayerPetitionPending(activeChar))
		{
			if (PetitionManager.getInstance().cancelActivePetition(activeChar))
			{
				int numRemaining = Config.MAX_PETITIONS_PER_PLAYER - PetitionManager.getInstance().getPlayerTotalPetitionCount(activeChar);
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PETITION_CANCELED_SUBMIT_S1_MORE_TODAY).addNumber(numRemaining));

				// Notify all GMs that the player's pending petition has been cancelled.
				String msgContent = activeChar.getName() + " has canceled a pending petition.";
				GmListTable.broadcastToGMs(new CreatureSay(activeChar.getObjectId(), SystemChatChannelId.Chat_Hero, "Petition System", msgContent));
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.FAILED_CANCEL_PETITION_TRY_LATER);
			}
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.PETITION_NOT_SUBMITTED);
		}
	}

}