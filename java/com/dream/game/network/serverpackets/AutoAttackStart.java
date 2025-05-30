package com.dream.game.network.serverpackets;

public class AutoAttackStart extends L2GameServerPacket
{
	private final int _targetObjId;

	public AutoAttackStart(int targetObjId)
	{
		_targetObjId = targetObjId;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x2b);
		writeD(_targetObjId);
	}

}