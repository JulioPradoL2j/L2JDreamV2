package com.dream.game.network.clientpackets;

import com.dream.game.manager.DuelManager;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;

public final class RequestDuelAnswerStart extends L2GameClientPacket
{
	private int _partyDuel;
	@SuppressWarnings("unused")
	private int _unk1;
	private int _response;

	@Override
	protected void readImpl()
	{
		_partyDuel = readD();
		_unk1 = readD();
		_response = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		L2PcInstance requestor = player.getActiveRequester();
		if (requestor == null)
			return;

		if (_response == 1)
		{
			if (requestor.isInDuel())
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_ALREADY_ENGAGED_IN_A_DUEL).addString(requestor.getName()));
				return;
			}
			else if (player.isInDuel())
			{
				player.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
				return;
			}

			if (_partyDuel == 1)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACCEPTED_S1S_CHALLENGE_TO_A_PARTY_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS).addString(requestor.getName()));
				requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_DUEL_AGAINST_THEIR_PARTY_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS).addString(player.getName()));
			}
			else
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACCEPTED_S1S_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS).addString(requestor.getName()));
				requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS).addString(player.getName()));
			}
			DuelManager.getInstance().addDuel(requestor, player, _partyDuel);
		}
		else if (_partyDuel == 1)
		{
			requestor.sendPacket(SystemMessageId.THE_OPPOSING_PARTY_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL);
		}
		else
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL).addString(player.getName()));
		}

		player.setActiveRequester(null);
		requestor.onTransactionResponse();
	}

}