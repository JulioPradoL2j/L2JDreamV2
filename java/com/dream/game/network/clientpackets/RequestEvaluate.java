package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.network.serverpackets.UserInfo;

public class RequestEvaluate extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _targetId;

	@Override
	protected void readImpl()
	{
		_targetId = readD();
	}

	@Override
	protected void runImpl()
	{

		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (!(activeChar.getTarget() instanceof L2PcInstance))
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}
		if (activeChar.getLevel() < 10)
		{
			activeChar.sendPacket(SystemMessageId.ONLY_LEVEL_SUP_10_CAN_RECOMMEND);
			return;
		}
		if (activeChar.getTarget() == activeChar)
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_RECOMMEND_YOURSELF);
			return;
		}
		if (activeChar.getRecomLeft() <= 0)
		{
			activeChar.sendPacket(SystemMessageId.NO_MORE_RECOMMENDATIONS_TO_HAVE);
			return;
		}

		L2PcInstance target = (L2PcInstance) activeChar.getTarget();
		if (target == null)
			return;

		if (target.getRecomHave() >= 255)
		{
			activeChar.sendPacket(SystemMessageId.YOUR_TARGET_NO_LONGER_RECEIVE_A_RECOMMENDATION);
			return;
		}
		if (!activeChar.canRecom(target))
		{
			activeChar.sendPacket(SystemMessageId.THAT_CHARACTER_IS_RECOMMENDED);
			return;
		}

		activeChar.giveRecom(target);

		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_RECOMMENDED_S1_YOU_ARE_AUTHORIZED_TO_MAKE_S2_MORE_RECOMMENDATIONS).addString(target.getName()).addNumber(activeChar.getRecomLeft()));
		target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_BEEN_RECOMMENDED_BY_S1).addString(activeChar.getName()));

		activeChar.sendPacket(new UserInfo(activeChar));
		target.broadcastUserInfo();
	}

}