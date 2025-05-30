package com.dream.game.network.serverpackets;

import com.dream.game.model.L2Object;

public class DeleteObject extends L2GameServerPacket
{
	private final int _objectId;

	public DeleteObject(L2Object obj)
	{
		_objectId = obj.getObjectId();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x12);
		writeD(_objectId);
		writeD(0x00);
	}

}