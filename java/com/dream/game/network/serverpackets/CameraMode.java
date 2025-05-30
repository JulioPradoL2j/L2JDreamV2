package com.dream.game.network.serverpackets;

public class CameraMode extends L2GameServerPacket
{
	private final int _mode;

	public CameraMode(int mode)
	{
		_mode = mode;
	}

	@Override
	public void writeImpl()
	{
		writeC(0xf1);
		writeD(_mode);
	}

}