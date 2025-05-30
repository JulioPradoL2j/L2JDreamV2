package com.dream.game.network.clientpackets;

import org.apache.log4j.Logger;

import com.dream.Config;

public class RequestPCCafeCouponUse extends L2GameClientPacket
{
	public static Logger _log = Logger.getLogger(RequestPCCafeCouponUse.class.getName());
	private String _str;

	@Override
	protected void readImpl()
	{
		_str = readS();
	}

	@Override
	protected void runImpl()
	{
		if (Config.DEBUG)
		{
			_log.debug("RequestPCCafeCouponUse " + _str);
		}
	}

}