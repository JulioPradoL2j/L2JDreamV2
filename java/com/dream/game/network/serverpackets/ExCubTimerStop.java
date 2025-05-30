package com.dream.game.network.serverpackets;

public final class ExCubTimerStop extends L2GameServerPacket
{
	public ExCubTimerStop()
	{
	}

	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0x8B);
	}

}