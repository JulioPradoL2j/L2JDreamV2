package com.dream.game.network.serverpackets;

import com.dream.game.model.actor.L2Character;

public class ChangeMoveType extends L2GameServerPacket
{
	public static final int WALK = 0;
	public static final int RUN = 1;

	private final int _chaObjId;
	private final boolean _running;

	public ChangeMoveType(L2Character character)
	{
		_chaObjId = character.getObjectId();
		_running = character.isRunning();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x2e);
		writeD(_chaObjId);
		writeD(_running ? RUN : WALK);
		writeD(0);
	}

}