package com.dream.game.network.clientpackets;

import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.AskJoinAlly;
import com.dream.game.network.serverpackets.SystemMessage;

public class RequestJoinAlly extends L2GameClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (activeChar.getClan() == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
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
		L2Clan clan = activeChar.getClan();
		if (!clan.checkAllyJoinCondition(activeChar, target))
			return;

		if (!activeChar.getRequest().setRequest(target, this))
			return;

		target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_ALLIANCE_LEADER_OF_S1_REQUESTED_ALLIANCE).addString(activeChar.getClan().getAllyName()).addString(activeChar.getName()));
		target.sendPacket(new AskJoinAlly(activeChar.getObjectId(), activeChar.getClan().getAllyName()));
		activeChar.sendPacket(SystemMessageId.YOU_INVITED_FOR_ALLIANCE);
	}

}