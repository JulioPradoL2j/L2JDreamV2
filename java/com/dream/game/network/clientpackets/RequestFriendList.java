package com.dream.game.network.clientpackets;

import com.dream.game.model.L2FriendList;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;

public class RequestFriendList extends L2GameClientPacket
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
		activeChar.sendPacket(SystemMessageId.FRIEND_LIST_HEADER);

		for (String friendName : L2FriendList.getFriendListNames(activeChar))
		{
			L2PcInstance friend = L2World.getInstance().getPlayer(friendName);

			if (friend == null)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_OFFLINE).addString(friendName));
			}
			else
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ONLINE).addString(friendName));
			}
		}
		activeChar.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
	}

}
