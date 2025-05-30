package com.dream.game.network.loginserverpackets;

import com.dream.game.network.IOFloodManager;

public class LoginNotify extends LoginServerBasePacket
{

	private final String _IP;

	public LoginNotify(byte[] decrypt)
	{
		super(decrypt);
		_IP = readS();
		IOFloodManager.addIp(_IP);
	}

}
