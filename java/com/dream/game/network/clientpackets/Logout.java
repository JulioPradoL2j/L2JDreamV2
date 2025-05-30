package com.dream.game.network.clientpackets;

import com.dream.game.communitybbs.Manager.RegionBBSManager;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.Disconnection;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.RestartResponse;

public final class Logout extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
			return;

		if (activeChar.isInArenaEvent() || activeChar.isArenaProtection())
		{
			activeChar.sendMessage("You cannot logout while in Tournament Event!");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.isInsideZone(L2Zone.FLAG_NORESTART) && !activeChar.isGM())
		{
			activeChar.sendPacket(SystemMessageId.NO_LOGOUT_HERE);
			sendPacket(RestartResponse.valueOf(false));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (!activeChar.canLogout())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		RegionBBSManager.getInstance().changeCommunityBoard();
		new Disconnection(getClient(), activeChar).defaultSequence(false);
	}

}