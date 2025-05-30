package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.ExDuelAskStart;
import com.dream.game.network.serverpackets.SystemMessage;

public final class RequestDuelStart extends L2GameClientPacket
{
	private String _player;
	private int _partyDuel;

	@Override
	protected void readImpl()
	{
		_player = readS();
		_partyDuel = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		if (activeChar.getSecondRefusal())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		L2PcInstance targetChar = L2World.getInstance().getPlayer(_player);
		if (targetChar == null)
		{
			activeChar.sendPacket(SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL);
			return;
		}

		if (activeChar == targetChar)
		{
			activeChar.sendPacket(SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL);
			return;
		}

		if (!activeChar.canDuel())
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
			return;
		}
		else if (!targetChar.canDuel())
		{
			activeChar.sendPacket(targetChar.getNoDuelReason());
			return;
		}
		else if (!activeChar.isInsideRadius(targetChar, 250, false, false))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_RECEIVE_A_DUEL_CHALLENGE_BECAUSE_S1_IS_TOO_FAR_AWAY).addString(targetChar.getName()));
			return;
		}
		if (_partyDuel == 1)
		{
			if (!activeChar.isInParty() || !(activeChar.isInParty() && activeChar.getParty().isLeader(activeChar)))
			{
				activeChar.sendMessage("You have to be the leader of a party in order to request a party duel.");
				return;
			}
			else if (!targetChar.isInParty())
			{
				activeChar.sendPacket(SystemMessageId.SINCE_THE_PERSON_YOU_CHALLENGED_IS_NOT_CURRENTLY_IN_A_PARTY_THEY_CANNOT_DUEL_AGAINST_YOUR_PARTY);
				return;
			}
			else if (activeChar.getParty().getPartyMembers().contains(targetChar))
			{
				activeChar.sendMessage("This player is a member of your own party.");
				return;
			}
			for (L2PcInstance temp : activeChar.getParty().getPartyMembers())
				if (!temp.canDuel())
				{
					activeChar.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
					return;
				}
			L2PcInstance partyLeader = null;

			for (L2PcInstance temp : targetChar.getParty().getPartyMembers())
			{
				if (partyLeader == null)
				{
					partyLeader = temp;
				}
				if (!temp.canDuel())
				{
					activeChar.sendPacket(SystemMessageId.THE_OPPOSING_PARTY_IS_CURRENTLY_UNABLE_TO_ACCEPT_A_CHALLENGE_TO_A_DUEL);
					return;
				}
			}
			if (partyLeader != null)
				if (!partyLeader.isProcessingRequest())
				{
					activeChar.onTransactionRequest(partyLeader);
					partyLeader.sendPacket(new ExDuelAskStart(activeChar.getName(), _partyDuel));

					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1S_PARTY_HAS_BEEN_CHALLENGED_TO_A_DUEL).addString(partyLeader.getName()));
					targetChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1S_PARTY_HAS_CHALLENGED_YOUR_PARTY_TO_A_DUEL).addString(activeChar.getName()));
				}
				else
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(partyLeader.getName()));
				}
		}
		else if (!targetChar.isProcessingRequest())
		{
			activeChar.onTransactionRequest(targetChar);
			targetChar.sendPacket(new ExDuelAskStart(activeChar.getName(), _partyDuel));
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_CHALLENGED_TO_A_DUEL).addString(targetChar.getName()));
			targetChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_CHALLENGED_YOU_TO_A_DUEL).addString(activeChar.getName()));
		}
		else
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(targetChar.getName()));
		}
	}

}