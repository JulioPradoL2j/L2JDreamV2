package com.dream.game.network.clientpackets;

import com.dream.game.manager.CastleManager;
import com.dream.game.model.entity.siege.Castle;
import com.dream.game.network.serverpackets.SiegeDefenderList;

public class RequestSiegeDefenderList extends L2GameClientPacket
{
	private int _castleId;

	@Override
	protected void readImpl()
	{
		_castleId = readD();
	}

	@Override
	protected void runImpl()
	{
		Castle castle = CastleManager.getInstance().getCastleById(_castleId);
		if (castle == null)
			return;

		sendPacket(new SiegeDefenderList(castle));
	}

}