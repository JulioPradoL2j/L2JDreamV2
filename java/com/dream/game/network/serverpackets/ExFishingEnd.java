package com.dream.game.network.serverpackets;

import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;

public class ExFishingEnd extends L2GameServerPacket
{
	private final boolean _win;
	L2Character _activeChar;

	public ExFishingEnd(boolean win, L2PcInstance character)
	{
		_win = win;
		_activeChar = character;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x14);
		writeD(_activeChar.getObjectId());
		writeC(_win ? 1 : 0);
	}

}