package com.dream.game.network.serverpackets;

public class AcquireSkillDone extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeC(0x25);
	}

}