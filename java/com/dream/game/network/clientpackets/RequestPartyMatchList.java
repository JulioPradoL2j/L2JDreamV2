package com.dream.game.network.clientpackets;

import com.dream.game.manager.PartyRoomManager;
import com.dream.game.model.L2Party;
import com.dream.game.model.L2PartyRoom;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;

public class RequestPartyMatchList extends L2GameClientPacket
{
	private int _lootDist;
	private int _maxMembers;
	private int _minLevel;
	private int _maxLevel;
	private int _roomId;
	private String _roomTitle;

	@Override
	protected void readImpl()
	{
		_roomId = readD();
		_maxMembers = readD();
		_minLevel = readD();
		_maxLevel = readD();
		_lootDist = readD();
		_roomTitle = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		L2Party party = activeChar.getParty();
		if (party != null && !party.isLeader(activeChar))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2PartyRoom room = activeChar.getPartyRoom();
		if (room == null)
		{
			PartyRoomManager.getInstance().createRoom(activeChar, _minLevel, _maxLevel, _maxMembers, _lootDist, _roomTitle);
			activeChar.sendPacket(SystemMessageId.PARTY_ROOM_CREATED);
		}
		else if (room.getId() == _roomId)
		{
			room.setLootDist(_lootDist);
			room.setMaxMembers(_maxMembers);
			room.setMinLevel(_minLevel);
			room.setMaxLevel(_maxLevel);
			room.setTitle(_roomTitle);
			room.updateRoomStatus(false);
			room.broadcastPacket(SystemMessageId.PARTY_ROOM_REVISED.getSystemMessage());
		}

		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	}

}