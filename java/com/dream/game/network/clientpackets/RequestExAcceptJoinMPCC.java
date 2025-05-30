package com.dream.game.network.clientpackets;

import com.dream.game.model.L2CommandChannel;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;

public class RequestExAcceptJoinMPCC extends L2GameClientPacket
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
		if (player != null)
		{
			L2PcInstance requestor = player.getActiveRequester();
			if (requestor == null)
				return;
			if (_response == 1)
			{
				boolean newCc = false;
				if (!requestor.getParty().isInCommandChannel())
				{
					new L2CommandChannel(requestor);
					newCc = true;
				}
				requestor.getParty().getCommandChannel().addParty(player.getParty());
				if (!newCc)
				{
					player.getParty().broadcastToPartyMembers(SystemMessage.getSystemMessage(SystemMessageId.JOINED_COMMAND_CHANNEL));
				}
			}
			else
			{
				requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DECLINED_CHANNEL_INVITATION).addPcName(player));
			}
			player.setActiveRequester(null);
			requestor.onTransactionResponse();
		}
	}

}