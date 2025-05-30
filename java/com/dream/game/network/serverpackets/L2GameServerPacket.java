package com.dream.game.network.serverpackets;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.network.L2GameClient;
import com.dream.mmocore.SendablePacket;

public abstract class L2GameServerPacket extends SendablePacket<L2GameClient>
{
	protected static final Logger _log = Logger.getLogger(L2GameServerPacket.class.getName());

	public String getType()
	{
		return "[S] " + getClass().getSimpleName();
	}

	public void runImpl()
	{

	}

	@Override
	protected void write()
	{
		if (Config.DEBUG)
		{
			_log.info(getType());
		}

		try
		{
			writeImpl();
		}
		catch (Throwable t)
		{
			_log.warn("Client: " + getClient().toString() + " - Failed writing: " + getType());
			t.printStackTrace();
		}
	}

	protected abstract void writeImpl();

}