package com.dream.game.network.serverpackets;

public class CharDeleteSuccess extends L2GameServerPacket
{
	public static final CharDeleteSuccess STATIC_PACKET = new CharDeleteSuccess();

	public CharDeleteSuccess()
	{

	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x23);
	}

}