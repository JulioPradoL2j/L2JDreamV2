package com.dream.game.network.clientpackets;

import com.dream.game.manager.CastleManager;
import com.dream.game.manager.ClanHallManager;
import com.dream.game.model.entity.ClanHall;
import com.dream.game.model.entity.siege.Castle;
import com.dream.game.network.serverpackets.SiegeAttackerList;

public class RequestSiegeAttackerList extends L2GameClientPacket
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
		if (castle != null)
		{
			sendPacket(new SiegeAttackerList(castle, null));
		}
		else
		{
			ClanHall clanHall = ClanHallManager.getInstance().getClanHallById(_castleId);
			if (clanHall != null)
			{
				sendPacket(new SiegeAttackerList(null, clanHall));
			}
		}
	}

}