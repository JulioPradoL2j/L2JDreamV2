package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.game.util.FloodProtector;
import com.dream.game.util.FloodProtector.Protected;
import com.dream.game.util.Util;

public class RequestSocialAction extends L2GameClientPacket
{
	private int _actionId;

	@Override
	protected void readImpl()
	{
		_actionId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			return;
		}

		if (_actionId < 2 || _actionId > 14)
		{
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + "tried to use a prohibited action.", Config.DEFAULT_PUNISH);
			return;
		}

		if (activeChar.getPrivateStoreType() == 0 && activeChar.getActiveRequester() == null && !activeChar.isAlikeDead() && !activeChar.isCastingNow() && !activeChar.isCastingSimultaneouslyNow() && (!activeChar.isAllSkillsDisabled() || activeChar.isInDuel()) && activeChar.getAI().getIntention() == CtrlIntention.IDLE && FloodProtector.tryPerformAction(activeChar, Protected.SOCIAL))
		{
			activeChar.broadcastPacket(new SocialAction(activeChar, _actionId));
		}
	}

}