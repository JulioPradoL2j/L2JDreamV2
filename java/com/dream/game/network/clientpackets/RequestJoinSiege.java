package com.dream.game.network.clientpackets;

import com.dream.game.manager.CastleManager;
import com.dream.game.manager.clanhallsiege.DevastatedCastleSiege;
import com.dream.game.manager.clanhallsiege.FortressOfDeadSiege;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.siege.Castle;
import com.dream.game.network.SystemMessageId;

public class RequestJoinSiege extends L2GameClientPacket
{
	private int _castleId;
	private int _isAttacker;
	private int _isJoining;

	private Castle castle;

	@Override
	protected void readImpl()
	{
		_castleId = readD();
		_isAttacker = readD();
		_isJoining = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		if (!activeChar.isClanLeader())
			return;

		castle = CastleManager.getInstance().getCastleById(_castleId);
		if (castle == null && _castleId != 34 && _castleId != 64)
			return;
		if (castle == null && _isAttacker == 0)
			return;
		if (_isJoining == 1)
		{
			if (System.currentTimeMillis() < activeChar.getClan().getDissolvingExpiryTime())
			{
				activeChar.sendPacket(SystemMessageId.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS);
				return;
			}
			if (_isAttacker == 1)
			{
				if (_castleId == 34)
				{
					DevastatedCastleSiege.getInstance().registerClan(activeChar);
				}
				else if (_castleId == 64)
				{
					FortressOfDeadSiege.getInstance().registerClan(activeChar);
				}
				else
				{
					castle.getSiege().registerAttacker(activeChar);
				}
				castle.getSiege().removeSiegeClan(activeChar);
			}
			else if (castle != null)
			{
				castle.getSiege().registerDefender(activeChar);
			}
			castle.getSiege().removeSiegeClan(activeChar);
		}
		else if (_castleId == 34)
		{
			FortressOfDeadSiege.getInstance().listRegisterClan(activeChar);
			DevastatedCastleSiege.getInstance().removeSiegeClan(activeChar);
		}
		else if (_castleId == 64)
		{
			FortressOfDeadSiege.getInstance().listRegisterClan(activeChar);
			FortressOfDeadSiege.getInstance().removeSiegeClan(activeChar);
		}
		else
		{
			castle.getSiege().listRegisterClan(activeChar);
			castle.getSiege().removeSiegeClan(activeChar);
		}
		if (_castleId == 34)
		{
			DevastatedCastleSiege.getInstance().listRegisterClan(activeChar);
		}
		else if (_castleId == 64)
		{
			FortressOfDeadSiege.getInstance().listRegisterClan(activeChar);
		}
		else
		{
			castle.getSiege().removeSiegeClan(activeChar);
			castle.getSiege().listRegisterClan(activeChar);
		}
	}

}