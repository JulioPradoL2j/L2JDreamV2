package com.dream.game.network.serverpackets;

public class CharCreateFail extends L2GameServerPacket
{
	public static final int REASON_CREATION_FAILED = 0x00;
	public static final int REASON_TOO_MANY_CHARACTERS = 0x01;
	public static final int REASON_NAME_ALREADY_EXISTS = 0x02;
	public static final int REASON_16_ENG_CHARS = 0x03;
	public static final int REASON_INCORRECT_NAME = 0x04;
	public static final int REASON_CREATE_NOT_ALLOWED = 0x05;
	public static final int REASON_CHOOSE_ANOTHER_SVR = 0x06;

	private final int _error;

	public CharCreateFail(int errorCode)
	{
		_error = errorCode;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x1a);
		writeD(_error);
	}

}