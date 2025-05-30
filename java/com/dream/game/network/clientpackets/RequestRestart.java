package com.dream.game.network.clientpackets;

import com.dream.game.communitybbs.Manager.RegionBBSManager;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.Disconnection;
import com.dream.game.network.L2GameClient;
import com.dream.game.network.L2GameClient.GameClientState;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.CharSelectionInfo;
import com.dream.game.network.serverpackets.RestartResponse;

public final class RequestRestart extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		final L2GameClient client = getClient();
		final L2PcInstance activeChar = client.getActiveChar();

		if (activeChar == null)
			return;

		if (activeChar.isInArenaEvent() || activeChar.isArenaProtection())
		{
			activeChar.sendMessage("You cannot logout while in Tournament Event!");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			sendPacket(RestartResponse.valueOf(false));
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
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.getActiveRequester() != null)
		{
			activeChar.getActiveRequester().onTradeCancel(activeChar);
			activeChar.onTradeCancel(activeChar.getActiveRequester());
		}

		RegionBBSManager.getInstance().changeCommunityBoard();
		activeChar._inWorld = false;
		new Disconnection(client, activeChar).deleteMe();

		client.setState(GameClientState.AUTHED);
		sendPacket(RestartResponse.valueOf(true));
		CharSelectionInfo cl = new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1);
		sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}

}