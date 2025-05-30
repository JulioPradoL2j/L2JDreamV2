package com.dream.game.network.serverpackets;

public final class ExShowCubTimer extends L2GameServerPacket
{
	private final int _x;

	public ExShowCubTimer(int x)
	{
		_x = x;
	}

	@Override
	public String getType()
	{
		return null;
	}

	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0x8A);
		// writeH(0x89);
		writeD(_x);
	}
}