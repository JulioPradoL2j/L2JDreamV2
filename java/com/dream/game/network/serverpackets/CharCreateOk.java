package com.dream.game.network.serverpackets;

public class CharCreateOk extends L2GameServerPacket
{
	public static final CharCreateOk STATIC_PACKET = new CharCreateOk();

	public CharCreateOk()
	{

	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x19);
		writeD(0x01);
	}

}