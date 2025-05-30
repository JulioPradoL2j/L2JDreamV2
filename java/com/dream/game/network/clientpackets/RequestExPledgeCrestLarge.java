package com.dream.game.network.clientpackets;

import com.dream.game.cache.CrestCache;
import com.dream.game.network.serverpackets.ExPledgeCrestLarge;

public class RequestExPledgeCrestLarge extends L2GameClientPacket
{
	private int _crestId;

	@Override
	protected void readImpl()
	{
		_crestId = readD();
	}

	@Override
	protected void runImpl()
	{
		byte[] data = CrestCache.getInstance().getPledgeCrestLarge(_crestId);

		if (data != null)
		{
			sendPacket(new ExPledgeCrestLarge(_crestId, data));
		}

	}

}