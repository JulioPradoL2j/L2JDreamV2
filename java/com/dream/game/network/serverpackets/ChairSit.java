package com.dream.game.network.serverpackets;

import com.dream.game.model.actor.instance.L2PcInstance;

public class ChairSit extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final int _staticObjectId;

	public ChairSit(L2PcInstance player, int staticObjectId)
	{
		_activeChar = player;
		_staticObjectId = staticObjectId;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xe1);
		writeD(_activeChar.getObjectId());
		writeD(_staticObjectId);
	}

}