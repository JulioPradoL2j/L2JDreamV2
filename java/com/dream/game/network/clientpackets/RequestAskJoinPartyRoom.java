package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.Shutdown;
import com.dream.game.model.L2PartyRoom;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.ExAskJoinPartyRoom;
import com.dream.game.network.serverpackets.SystemMessage;

public class RequestAskJoinPartyRoom extends L2GameClientPacket
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

		if (Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_TRANSACTION && Shutdown.getCounterInstance() != null && Shutdown.getCounterInstance().getCountdown() <= Config.SAFE_REBOOT_TIME)
		{
			activeChar.sendPacket(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2PartyRoom room = activeChar.getPartyRoom();
		L2PcInstance target = L2World.getInstance().getPlayer(_name);
		if (target == null || target == activeChar || room == null)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		else if (target.getPartyRoom() != null)
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_NOT_MEET_CONDITIONS_FOR_PARTY_ROOM).addString(_name));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		else if (activeChar != room.getLeader())
		{
			activeChar.sendPacket(SystemMessageId.ONLY_ROOM_LEADER_CAN_INVITE);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		else if (room.getMemberCount() >= room.getMaxMembers())
		{
			activeChar.sendPacket(SystemMessageId.PARTY_ROOM_FULL);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		else if (activeChar.isProcessingRequest())
		{
			activeChar.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		else if (target.isProcessingRequest())
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(_name));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		activeChar.onTransactionRequest(target);
		target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_INVITED_YOU_TO_PARTY_ROOM).addPcName(activeChar));
		target.sendPacket(new ExAskJoinPartyRoom(activeChar.getName()));

		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	}

}