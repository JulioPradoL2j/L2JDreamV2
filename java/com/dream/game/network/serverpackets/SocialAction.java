package com.dream.game.network.serverpackets;

import com.dream.game.model.actor.L2Character;

public class SocialAction extends L2GameServerPacket
{
	private final int _charObjId, _actionId;

	public SocialAction(L2Character cha, int actionId)
	{
		_charObjId = cha.getObjectId();
		_actionId = actionId;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x2d);
		writeD(_charObjId);
		writeD(_actionId);
	}

}