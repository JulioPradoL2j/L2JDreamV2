package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;

public class RequestExOustFromMPCC extends L2GameClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance target = L2World.getInstance().getPlayer(_name);
		L2PcInstance activeChar = getClient().getActiveChar();

		if (target != null && target.isInParty() && activeChar.isInParty() && activeChar.getParty().isInCommandChannel() && target.getParty().isInCommandChannel() && activeChar.getParty().getCommandChannel().getChannelLeader().equals(activeChar))
		{
			target.getParty().getCommandChannel().removeParty(target.getParty());

			SystemMessage sm = SystemMessage.sendString("Your group, left the team.");
			target.getParty().broadcastToPartyMembers(sm);

			sm = SystemMessage.sendString(target.getParty().getPartyMembers().get(0).getName() + "'s party was dismissed from the CommandChannel.");
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
		}
	}

}