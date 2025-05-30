package com.dream.game.network.clientpackets;

import com.dream.game.manager.PartyRoomManager;
import com.dream.game.model.L2Party;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.ListPartyWaiting;

public class RequestPartyMatchConfig extends L2GameClientPacket
{
	private int _page;
	private int _region;
	private boolean _showClass;

	@Override
	protected void readImpl()
	{
		_page = readD();
		_region = readD();
		_showClass = readD() == 1;
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		L2Party party = player.getParty();
		if (party != null && !party.isLeader(player))
		{
			player.sendPacket(SystemMessageId.CANT_VIEW_PARTY_ROOMS);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		player.setPartyMatchingShowClass(_showClass);
		player.setPartyMatchingRegion(_region);

		PartyRoomManager.getInstance().addToWaitingList(player);
		sendPacket(new ListPartyWaiting(player, _page));

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

}