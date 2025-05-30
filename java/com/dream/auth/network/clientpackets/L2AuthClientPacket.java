package com.dream.auth.network.clientpackets;

import org.apache.log4j.Logger;

import com.dream.auth.L2AuthClient;
import com.dream.mmocore.ReceivablePacket;

public abstract class L2AuthClientPacket extends ReceivablePacket<L2AuthClient>
{
	private static Logger _log = Logger.getLogger(L2AuthClientPacket.class.getName());

	@Override
	protected final boolean read()
	{
		try
		{
			return readImpl();
		}
		catch (Exception e)
		{
			_log.fatal("ERROR READING: " + this.getClass().getSimpleName(), e);
			return false;
		}
	}

	protected abstract boolean readImpl();
}