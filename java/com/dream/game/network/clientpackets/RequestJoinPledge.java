package com.dream.game.network.clientpackets;

import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.AskJoinPledge;

public class RequestJoinPledge extends L2GameClientPacket
{
	private int _objectId;
	private int _pledgeType;
	private String _pledgeName;
	private String _subPledgeName;

	public int getPledgeType()
	{
		return _pledgeType;
	}

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_pledgeType = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		L2Clan clan = activeChar.getClan();
		if (clan == null)
		{
			activeChar.sendPacket(SystemMessageId.NOT_JOINED_IN_ANY_CLAN);
			return;
		}

		L2Object obj = null;

		if (activeChar.getTargetId() == _objectId)
		{
			obj = activeChar.getTarget();
		}

		if (obj == null)
		{
			obj = L2World.getInstance().getPlayer(_objectId);
		}

		if (!(obj instanceof L2PcInstance))
		{
			activeChar.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return;
		}

		L2PcInstance target = (L2PcInstance) obj;
		if (!clan.checkClanJoinCondition(activeChar, target, _pledgeType))
			return;

		if (!activeChar.getRequest().setRequest(target, this))
			return;

		_pledgeName = activeChar.getClan().getName();
		_subPledgeName = activeChar.getClan().getSubPledge(_pledgeType) != null ? activeChar.getClan().getSubPledge(_pledgeType).getName() : null;
		target.sendPacket(new AskJoinPledge(activeChar.getObjectId(), _subPledgeName, _pledgeType, _pledgeName));
	}

}