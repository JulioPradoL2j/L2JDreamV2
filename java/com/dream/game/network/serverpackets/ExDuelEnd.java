package com.dream.game.network.serverpackets;

public class ExDuelEnd extends L2GameServerPacket
{
	private final int _unk1;

	public ExDuelEnd(int unk1)
	{
		_unk1 = unk1;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x4e);

		writeD(_unk1);
	}

}