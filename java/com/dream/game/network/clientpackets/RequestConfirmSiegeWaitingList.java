package com.dream.game.network.clientpackets;

import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.manager.CastleManager;
import com.dream.game.model.L2Clan;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.siege.Castle;
import com.dream.game.network.serverpackets.SiegeDefenderList;

public class RequestConfirmSiegeWaitingList extends L2GameClientPacket
{
	private int _approved, _castleId, _clanId;

	@Override
	protected void readImpl()
	{
		_castleId = readD();
		_clanId = readD();
		_approved = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		Castle castle = CastleManager.getInstance().getCastleById(_castleId);
		L2Clan clan = ClanTable.getInstance().getClan(_clanId);

		if (activeChar == null)
			return;

		if (activeChar.getClan() == null)
			return;
		if (castle == null)
			return;

		if (castle.getOwnerId() != activeChar.getClanId() || !activeChar.isClanLeader())
			return;
		if (clan == null)
			return;
		if (!castle.getSiege().getIsRegistrationOver())
			if (_approved == 1)
			{
				if (castle.getSiege().checkIsDefenderWaiting(clan))
				{
					castle.getSiege().approveSiegeDefenderClan(_clanId);
				}
				else
					return;
			}
			else if (castle.getSiege().checkIsDefenderWaiting(clan) || castle.getSiege().checkIsDefender(clan))
			{
				castle.getSiege().removeSiegeClan(_clanId);
			}
		activeChar.sendPacket(new SiegeDefenderList(castle));
	}

}