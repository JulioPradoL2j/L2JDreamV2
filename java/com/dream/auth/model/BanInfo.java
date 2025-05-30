package com.dream.auth.model;

import com.dream.tools.network.SubNet;

public class BanInfo
{
	private final SubNet _net;

	private long _expiration;

	public BanInfo(SubNet net, long expiration)
	{
		_net = net;
		_expiration = expiration;
	}

	public SubNet getNet()
	{
		return _net;
	}

	public boolean hasExpired()
	{
		return System.currentTimeMillis() > _expiration;
	}

	public boolean isBanEternal()
	{
		return _expiration == 0;
	}

	public void setExpired(long val)
	{
		_expiration = val;
	}
}