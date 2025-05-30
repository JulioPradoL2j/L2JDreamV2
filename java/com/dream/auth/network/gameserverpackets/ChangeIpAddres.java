package com.dream.auth.network.gameserverpackets;

import com.dream.auth.network.clientpackets.ClientBasePacket;

public class ChangeIpAddres extends ClientBasePacket
{

	private final String _iphost;
	private final String _account;

	public ChangeIpAddres(byte[] decrypt)
	{
		super(decrypt);
		_iphost = readS();
		_account = readS();
	}

	public String getAccount()
	{
		return _account;
	}

	public String getIpHost()
	{
		return _iphost;
	}
}