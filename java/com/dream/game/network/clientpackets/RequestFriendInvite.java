package com.dream.game.network.clientpackets;

import com.dream.game.model.BlockList;
import com.dream.game.model.L2FriendList;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.FriendAddRequest;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.util.FloodProtector;
import com.dream.game.util.FloodProtector.Protected;

public class RequestFriendInvite extends L2GameClientPacket
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
		if (!FloodProtector.tryPerformAction(activeChar, Protected.CL_PACKET))
			return;

		L2PcInstance friend = L2World.getInstance().getPlayer(_name);

		if (friend == null)
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
		}
		else if (friend == activeChar)
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST);
		}
		else if (L2FriendList.isInFriendList(activeChar, friend))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_ON_LIST).addString(_name));
		}
		else if (friend.isInCombat())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(_name));
		}
		else if (BlockList.isBlocked(friend, activeChar))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(_name));
		}
		else if (friend.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(_name));
		}
		else if (!friend.isProcessingRequest())
		{
			activeChar.onTransactionRequest(friend);
			friend.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_REQUESTED_TO_BECOME_FRIENDS).addString(activeChar.getName()));
			friend.sendPacket(new FriendAddRequest(activeChar.getName()));
		}
		else
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(_name));
		}
	}

}