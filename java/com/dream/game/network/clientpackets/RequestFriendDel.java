package com.dream.game.network.clientpackets;

import com.dream.game.model.L2FriendList;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.SystemMessage;

public class RequestFriendDel extends L2GameClientPacket
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
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
			return;
		if (activeChar.getSecondRefusal())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		L2PcInstance friend = L2World.getInstance().getPlayer(_name);

		if (friend == activeChar)
			return;
		else if (!L2FriendList.isInFriendList(activeChar, _name))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_NOT_ON_YOUR_FRIENDS_LIST).addString(_name));
		}
		else if (friend != null)
		{
			L2FriendList.removeFromFriendList(activeChar, friend);
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(_name));
			friend.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(activeChar.getName()));
		}
		else
		{
			L2FriendList.removeFromFriendList(activeChar, _name);
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(_name));
		}
	}

}