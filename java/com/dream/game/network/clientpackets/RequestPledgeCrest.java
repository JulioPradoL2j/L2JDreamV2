package com.dream.game.network.clientpackets;

import com.dream.game.cache.CrestCache;
import com.dream.game.network.serverpackets.PledgeCrest;

public class RequestPledgeCrest extends L2GameClientPacket
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
		if (_crestId == 0)
			return;

		byte[] data = CrestCache.getInstance().getPledgeCrest(_crestId);

		if (data != null)
		{
			sendPacket(new PledgeCrest(_crestId, data));
		}
	}

}